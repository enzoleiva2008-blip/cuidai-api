-- =====================================================================
-- CuidAi - "Seu cuidado marcado, sem fila."
-- Global Solution 2026/2 - CitizenTech - Building Relational Database
-- Banco: Oracle (FIAP)
--
-- Ordem do script:
--   0. Limpeza (permite rodar o script de novo do zero)
--   1. Criacao das tabelas e sequences (na ordem das chaves estrangeiras)
--   2. INSERTs (60 registros)
--   3. 10 consultas numeradas
--   4. UPDATE com WHERE
--   5. DELETE com WHERE
--
-- Como rodar no SQL Developer: abra este arquivo e use "Executar Script" (F5).
-- =====================================================================


-- =====================================================================
-- 0. LIMPEZA
-- Apaga as tabelas e sequences do CuidAi, se ja existirem.
-- Na primeira execucao nao apaga nada e nao da erro.
-- =====================================================================
BEGIN
    FOR t IN (SELECT table_name FROM user_tables
               WHERE table_name IN ('TB_LEMBRETE', 'TB_CONSULTA', 'TB_CIDADAO',
                                    'TB_MEDICO_ESPECIALIDADE', 'TB_MEDICO',
                                    'TB_ESPECIALIDADE', 'TB_UBS')) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
    END LOOP;

    FOR s IN (SELECT sequence_name FROM user_sequences
               WHERE sequence_name IN ('SQ_TB_LEMBRETE', 'SQ_TB_CONSULTA', 'SQ_TB_CIDADAO',
                                       'SQ_TB_MEDICO', 'SQ_TB_ESPECIALIDADE', 'SQ_TB_UBS')) LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
    END LOOP;
END;
/


-- =====================================================================
-- 1. CRIACAO DAS TABELAS
-- Primeiro as tabelas sem chave estrangeira, depois as que dependem delas.
-- =====================================================================

-- ---------------------------------------------------------------------
-- TB_UBS: unidades basicas de saude atendidas pelo CuidAi
-- ---------------------------------------------------------------------
CREATE TABLE tb_ubs (
    cd_ubs        NUMBER(6)     CONSTRAINT pk_tb_ubs PRIMARY KEY,
    nm_ubs        VARCHAR2(80)  NOT NULL CONSTRAINT uk_tb_ubs_nome UNIQUE,
    ds_endereco   VARCHAR2(120) NOT NULL,
    nm_bairro     VARCHAR2(60)  NOT NULL,
    nm_cidade     VARCHAR2(60)  NOT NULL,
    sg_uf         CHAR(2)       NOT NULL,
    nr_telefone   VARCHAR2(15)
);
COMMENT ON TABLE tb_ubs IS 'Unidades Basicas de Saude onde o cidadao pode agendar consultas pelo CuidAi';

CREATE SEQUENCE sq_tb_ubs START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- TB_ESPECIALIDADE: especialidades medicas oferecidas
-- ---------------------------------------------------------------------
CREATE TABLE tb_especialidade (
    cd_especialidade  NUMBER(4)    CONSTRAINT pk_tb_especialidade PRIMARY KEY,
    nm_especialidade  VARCHAR2(60) NOT NULL CONSTRAINT uk_tb_especialidade_nome UNIQUE,
    nr_duracao_min    NUMBER(3)    NOT NULL
                      CONSTRAINT ck_tb_especialidade_duracao CHECK (nr_duracao_min > 0)
);
COMMENT ON TABLE tb_especialidade IS 'Especialidades medicas e a duracao media de uma consulta em minutos';

CREATE SEQUENCE sq_tb_especialidade START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- TB_MEDICO: medicos que atendem em uma UBS
-- Relacionamento 1:N -> uma UBS tem varios medicos
-- ---------------------------------------------------------------------
CREATE TABLE tb_medico (
    cd_medico   NUMBER(6)    CONSTRAINT pk_tb_medico PRIMARY KEY,
    cd_ubs      NUMBER(6)    NOT NULL,
    nm_medico   VARCHAR2(80) NOT NULL,
    nr_crm      VARCHAR2(15) NOT NULL CONSTRAINT uk_tb_medico_crm UNIQUE,
    fl_ativo    CHAR(1)      DEFAULT 'S' NOT NULL
                CONSTRAINT ck_tb_medico_ativo CHECK (fl_ativo IN ('S', 'N')),
    CONSTRAINT fk_tb_medico_ubs FOREIGN KEY (cd_ubs) REFERENCES tb_ubs (cd_ubs)
);
COMMENT ON TABLE tb_medico IS 'Medicos vinculados a uma UBS; fl_ativo indica se ainda recebe agendamentos';

CREATE SEQUENCE sq_tb_medico START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- TB_MEDICO_ESPECIALIDADE: tabela associativa do relacionamento N:N
-- Um medico atende varias especialidades e uma especialidade tem varios medicos
-- ---------------------------------------------------------------------
CREATE TABLE tb_medico_especialidade (
    cd_medico         NUMBER(6) NOT NULL,
    cd_especialidade  NUMBER(4) NOT NULL,
    CONSTRAINT pk_tb_medico_especialidade PRIMARY KEY (cd_medico, cd_especialidade),
    CONSTRAINT fk_tb_med_esp_medico FOREIGN KEY (cd_medico)
        REFERENCES tb_medico (cd_medico),
    CONSTRAINT fk_tb_med_esp_especialidade FOREIGN KEY (cd_especialidade)
        REFERENCES tb_especialidade (cd_especialidade)
);
COMMENT ON TABLE tb_medico_especialidade IS 'Associativa N:N entre medico e especialidade';

-- ---------------------------------------------------------------------
-- TB_CIDADAO: cidadaos que usam o CuidAi
-- A idade NAO e armazenada: e calculada a partir de dt_nascimento
-- ---------------------------------------------------------------------
CREATE TABLE tb_cidadao (
    cd_cidadao     NUMBER(8)     CONSTRAINT pk_tb_cidadao PRIMARY KEY,
    nm_cidadao     VARCHAR2(80)  NOT NULL,
    nr_cpf         CHAR(11)      NOT NULL CONSTRAINT uk_tb_cidadao_cpf UNIQUE,
    nr_cartao_sus  CHAR(15)      NOT NULL CONSTRAINT uk_tb_cidadao_sus UNIQUE,
    dt_nascimento  DATE          NOT NULL,
    nr_telefone    VARCHAR2(15)  NOT NULL,
    ds_email       VARCHAR2(100),
    fl_gestante    CHAR(1)       DEFAULT 'N' NOT NULL
                   CONSTRAINT ck_tb_cidadao_gestante CHECK (fl_gestante IN ('S', 'N')),
    fl_cronico     CHAR(1)       DEFAULT 'N' NOT NULL
                   CONSTRAINT ck_tb_cidadao_cronico CHECK (fl_cronico IN ('S', 'N'))
);
COMMENT ON TABLE tb_cidadao IS 'Cidadaos cadastrados; gestante e cronico definem prioridade na fila';

CREATE SEQUENCE sq_tb_cidadao START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- TB_CONSULTA: consultas agendadas pelos cidadaos
-- Relacionamento 1:N -> um cidadao tem varias consultas
-- Prioridade: 1 = Alta (idoso 60+ ou gestante), 2 = Media (cronico), 3 = Normal
-- ---------------------------------------------------------------------
CREATE TABLE tb_consulta (
    cd_consulta       NUMBER(10)   CONSTRAINT pk_tb_consulta PRIMARY KEY,
    cd_cidadao        NUMBER(8)    NOT NULL,
    cd_medico         NUMBER(6)    NOT NULL,
    cd_especialidade  NUMBER(4)    NOT NULL,
    dt_agendamento    DATE         DEFAULT SYSDATE NOT NULL,
    dt_consulta       DATE         NOT NULL,
    ds_status         VARCHAR2(10) DEFAULT 'AGENDADA' NOT NULL
                      CONSTRAINT ck_tb_consulta_status
                      CHECK (ds_status IN ('AGENDADA', 'REALIZADA', 'FALTOU', 'CANCELADA')),
    nr_prioridade     NUMBER(1)    NOT NULL
                      CONSTRAINT ck_tb_consulta_prioridade CHECK (nr_prioridade BETWEEN 1 AND 3),
    nr_espera_min     NUMBER(4)
                      CONSTRAINT ck_tb_consulta_espera CHECK (nr_espera_min >= 0),
    CONSTRAINT ck_tb_consulta_datas CHECK (dt_consulta >= dt_agendamento),
    CONSTRAINT fk_tb_consulta_cidadao FOREIGN KEY (cd_cidadao)
        REFERENCES tb_cidadao (cd_cidadao),
    CONSTRAINT fk_tb_consulta_medico FOREIGN KEY (cd_medico)
        REFERENCES tb_medico (cd_medico),
    CONSTRAINT fk_tb_consulta_especialidade FOREIGN KEY (cd_especialidade)
        REFERENCES tb_especialidade (cd_especialidade)
);
COMMENT ON TABLE tb_consulta IS 'Consultas agendadas; status e espera alimentam a IA de faltas e de tempo de espera';

CREATE SEQUENCE sq_tb_consulta START WITH 1 INCREMENT BY 1 NOCACHE;

-- ---------------------------------------------------------------------
-- TB_LEMBRETE: lembretes enviados pelo chatbot
-- Relacionamento 1:N -> uma consulta tem varios lembretes
-- ---------------------------------------------------------------------
CREATE TABLE tb_lembrete (
    cd_lembrete    NUMBER(10)    CONSTRAINT pk_tb_lembrete PRIMARY KEY,
    cd_consulta    NUMBER(10)    NOT NULL,
    dt_envio       DATE          NOT NULL,
    ds_canal       VARCHAR2(10)  NOT NULL
                   CONSTRAINT ck_tb_lembrete_canal CHECK (ds_canal IN ('WHATSAPP', 'SMS', 'EMAIL')),
    ds_mensagem    VARCHAR2(200) NOT NULL,
    fl_confirmado  CHAR(1)       DEFAULT 'N' NOT NULL
                   CONSTRAINT ck_tb_lembrete_confirmado CHECK (fl_confirmado IN ('S', 'N')),
    CONSTRAINT fk_tb_lembrete_consulta FOREIGN KEY (cd_consulta)
        REFERENCES tb_consulta (cd_consulta)
);
COMMENT ON TABLE tb_lembrete IS 'Lembretes do chatbot CuidAi e se o cidadao confirmou presenca';

CREATE SEQUENCE sq_tb_lembrete START WITH 1 INCREMENT BY 1 NOCACHE;


-- =====================================================================
-- 2. INSERTS - 60 registros no total
--    UBS 5 + Especialidade 5 + Medico 8 + Medico_Especialidade 10
--    + Cidadao 10 + Consulta 14 + Lembrete 8 = 60
-- Os dados sao ficticios. Textos sem acento para evitar problema de encoding.
-- =====================================================================

-- TB_UBS (5)
INSERT INTO tb_ubs VALUES (sq_tb_ubs.NEXTVAL, 'UBS Vila Mariana', 'Rua Domingos de Morais, 1200', 'Vila Mariana', 'Sao Paulo', 'SP', '1155550101');
INSERT INTO tb_ubs VALUES (sq_tb_ubs.NEXTVAL, 'UBS Jardim Sao Luis', 'Estrada do M Boi Mirim, 850', 'Jardim Sao Luis', 'Sao Paulo', 'SP', '1155550102');
INSERT INTO tb_ubs VALUES (sq_tb_ubs.NEXTVAL, 'UBS Mooca', 'Rua da Mooca, 2300', 'Mooca', 'Sao Paulo', 'SP', '1155550103');
INSERT INTO tb_ubs VALUES (sq_tb_ubs.NEXTVAL, 'UBS Lapa', 'Rua Guaicurus, 400', 'Lapa', 'Sao Paulo', 'SP', '1155550104');
INSERT INTO tb_ubs VALUES (sq_tb_ubs.NEXTVAL, 'UBS Capao Redondo', 'Rua Comendador Antunes dos Santos, 90', 'Capao Redondo', 'Sao Paulo', 'SP', NULL);

-- TB_ESPECIALIDADE (5) - Dermatologia fica sem medico de proposito (aparece no RIGHT JOIN)
INSERT INTO tb_especialidade VALUES (sq_tb_especialidade.NEXTVAL, 'Clinica Geral', 20);
INSERT INTO tb_especialidade VALUES (sq_tb_especialidade.NEXTVAL, 'Pediatria', 25);
INSERT INTO tb_especialidade VALUES (sq_tb_especialidade.NEXTVAL, 'Ginecologia e Obstetricia', 30);
INSERT INTO tb_especialidade VALUES (sq_tb_especialidade.NEXTVAL, 'Cardiologia', 30);
INSERT INTO tb_especialidade VALUES (sq_tb_especialidade.NEXTVAL, 'Dermatologia', 20);

-- TB_MEDICO (8)
INSERT INTO tb_medico VALUES (sq_tb_medico.NEXTVAL, 1, 'Ana Souza',        'CRM-SP 100001', 'S');
INSERT INTO tb_medico VALUES (sq_tb_medico.NEXTVAL, 1, 'Bruno Lima',       'CRM-SP 100002', 'S');
INSERT INTO tb_medico VALUES (sq_tb_medico.NEXTVAL, 2, 'Carla Mendes',     'CRM-SP 100003', 'S');
INSERT INTO tb_medico VALUES (sq_tb_medico.NEXTVAL, 2, 'Diego Rocha',      'CRM-SP 100004', 'S');
INSERT INTO tb_medico VALUES (sq_tb_medico.NEXTVAL, 3, 'Elisa Martins',    'CRM-SP 100005', 'S');
INSERT INTO tb_medico VALUES (sq_tb_medico.NEXTVAL, 4, 'Fabio Nunes',      'CRM-SP 100006', 'S');
INSERT INTO tb_medico VALUES (sq_tb_medico.NEXTVAL, 5, 'Gabriela Torres',  'CRM-SP 100007', 'S');
INSERT INTO tb_medico VALUES (sq_tb_medico.NEXTVAL, 3, 'Henrique Alves',   'CRM-SP 100008', 'N');

-- TB_MEDICO_ESPECIALIDADE (10)
INSERT INTO tb_medico_especialidade VALUES (1, 1);  -- Ana: Clinica Geral
INSERT INTO tb_medico_especialidade VALUES (1, 3);  -- Ana: Ginecologia
INSERT INTO tb_medico_especialidade VALUES (2, 1);  -- Bruno: Clinica Geral
INSERT INTO tb_medico_especialidade VALUES (2, 4);  -- Bruno: Cardiologia
INSERT INTO tb_medico_especialidade VALUES (3, 2);  -- Carla: Pediatria
INSERT INTO tb_medico_especialidade VALUES (4, 1);  -- Diego: Clinica Geral
INSERT INTO tb_medico_especialidade VALUES (5, 1);  -- Elisa: Clinica Geral
INSERT INTO tb_medico_especialidade VALUES (5, 3);  -- Elisa: Ginecologia
INSERT INTO tb_medico_especialidade VALUES (6, 4);  -- Fabio: Cardiologia
INSERT INTO tb_medico_especialidade VALUES (7, 2);  -- Gabriela: Pediatria

-- TB_CIDADAO (10) - Rafael (10) nao tem consulta de proposito (aparece no LEFT JOIN)
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Maria Aparecida Silva', '40312567801', '700100000000001', TO_DATE('1950-03-12', 'YYYY-MM-DD'), '11987650001', 'maria.silva@email.com',   'N', 'S');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Jose Carlos Pereira',   '40312567802', '700100000000002', TO_DATE('1958-07-25', 'YYYY-MM-DD'), '11987650002', NULL,                      'N', 'N');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Juliana Costa',         '40312567803', '700100000000003', TO_DATE('1996-02-10', 'YYYY-MM-DD'), '11987650003', 'juliana.costa@email.com', 'S', 'N');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Pedro Henrique Santos', '40312567804', '700100000000004', TO_DATE('1989-11-30', 'YYYY-MM-DD'), '11987650004', 'pedro.santos@email.com',  'N', 'S');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Ana Beatriz Oliveira',  '40312567805', '700100000000005', TO_DATE('2001-05-18', 'YYYY-MM-DD'), '11987650005', 'ana.oliveira@email.com',  'N', 'N');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Lucas Ferreira',        '40312567806', '700100000000006', TO_DATE('2019-08-03', 'YYYY-MM-DD'), '11987650006', NULL,                      'N', 'N');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Fernanda Ribeiro',      '40312567807', '700100000000007', TO_DATE('1999-12-22', 'YYYY-MM-DD'), '11987650007', 'fernanda.r@email.com',    'S', 'N');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Antonio Gomes',         '40312567808', '700100000000008', TO_DATE('1945-01-15', 'YYYY-MM-DD'), '11987650008', NULL,                      'N', 'S');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Camila Rodrigues',      '40312567809', '700100000000009', TO_DATE('1985-04-07', 'YYYY-MM-DD'), '11987650009', 'camila.rod@email.com',    'N', 'S');
INSERT INTO tb_cidadao VALUES (sq_tb_cidadao.NEXTVAL, 'Rafael Almeida',        '40312567810', '700100000000010', TO_DATE('2003-09-09', 'YYYY-MM-DD'), '11987650010', 'rafael.almeida@email.com','N', 'N');

-- TB_CONSULTA (14)
-- Colunas: cd, cidadao, medico, especialidade, agendamento, consulta, status, prioridade, espera
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 1, 1, 1, TO_DATE('2026-09-28 10:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-05 08:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 1, 15);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 2, 2, 4, TO_DATE('2026-09-20 14:30', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-06 09:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 1, 25);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 3, 1, 3, TO_DATE('2026-10-01 09:15', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-07 10:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 1, 10);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 4, 4, 1, TO_DATE('2026-09-15 16:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-08 08:30', 'YYYY-MM-DD HH24:MI'), 'FALTOU',    2, NULL);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 5, 5, 1, TO_DATE('2026-10-02 11:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-09 14:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 3, 55);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 6, 3, 2, TO_DATE('2026-09-10 08:45', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-13 15:00', 'YYYY-MM-DD HH24:MI'), 'FALTOU',    3, NULL);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 7, 5, 3, TO_DATE('2026-10-05 13:20', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-14 09:30', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 1, 20);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 8, 6, 4, TO_DATE('2026-10-01 10:10', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-15 08:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 1, 30);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 9, 2, 1, TO_DATE('2026-10-10 17:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-20 11:00', 'YYYY-MM-DD HH24:MI'), 'CANCELADA', 2, NULL);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 1, 6, 4, TO_DATE('2026-10-20 09:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-10-27 10:00', 'YYYY-MM-DD HH24:MI'), 'REALIZADA', 1, 18);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 6, 7, 2, TO_DATE('2026-10-28 15:40', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-10 09:00', 'YYYY-MM-DD HH24:MI'), 'AGENDADA',  3, NULL);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 2, 1, 1, TO_DATE('2026-11-01 08:30', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-12 08:00', 'YYYY-MM-DD HH24:MI'), 'AGENDADA',  1, NULL);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 3, 5, 3, TO_DATE('2026-11-02 12:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-12 10:30', 'YYYY-MM-DD HH24:MI'), 'AGENDADA',  1, NULL);
INSERT INTO tb_consulta VALUES (sq_tb_consulta.NEXTVAL, 9, 2, 1, TO_DATE('2026-11-03 19:00', 'YYYY-MM-DD HH24:MI'), TO_DATE('2026-11-12 08:30', 'YYYY-MM-DD HH24:MI'), 'AGENDADA',  2, NULL);

-- TB_LEMBRETE (8)
INSERT INTO tb_lembrete VALUES (sq_tb_lembrete.NEXTVAL, 1,  TO_DATE('2026-10-04 08:00', 'YYYY-MM-DD HH24:MI'), 'WHATSAPP', 'Ola, Maria! Sua consulta e amanha as 08:00 na UBS Vila Mariana. Responda 1 para confirmar.', 'S');
INSERT INTO tb_lembrete VALUES (sq_tb_lembrete.NEXTVAL, 2,  TO_DATE('2026-10-05 09:00', 'YYYY-MM-DD HH24:MI'), 'WHATSAPP', 'Ola, Jose! Sua consulta de Cardiologia e amanha as 09:00. Responda 1 para confirmar.', 'S');
INSERT INTO tb_lembrete VALUES (sq_tb_lembrete.NEXTVAL, 4,  TO_DATE('2026-10-07 08:30', 'YYYY-MM-DD HH24:MI'), 'SMS',      'CuidAi: sua consulta e amanha as 08:30 na UBS Jardim Sao Luis.', 'N');
INSERT INTO tb_lembrete VALUES (sq_tb_lembrete.NEXTVAL, 6,  TO_DATE('2026-10-12 15:00', 'YYYY-MM-DD HH24:MI'), 'WHATSAPP', 'Ola! A consulta de Pediatria do Lucas e amanha as 15:00. Responda 1 para confirmar.', 'N');
INSERT INTO tb_lembrete VALUES (sq_tb_lembrete.NEXTVAL, 9,  TO_DATE('2026-10-19 11:00', 'YYYY-MM-DD HH24:MI'), 'WHATSAPP', 'Ola, Camila! Sua consulta e amanha as 11:00 na UBS Vila Mariana. Responda 1 para confirmar.', 'N');
INSERT INTO tb_lembrete VALUES (sq_tb_lembrete.NEXTVAL, 11, TO_DATE('2026-10-28 15:45', 'YYYY-MM-DD HH24:MI'), 'WHATSAPP', 'Consulta de Pediatria agendada para 10/11 as 09:00 na UBS Capao Redondo.', 'S');
INSERT INTO tb_lembrete VALUES (sq_tb_lembrete.NEXTVAL, 12, TO_DATE('2026-11-01 08:35', 'YYYY-MM-DD HH24:MI'), 'EMAIL',    'CuidAi: consulta confirmada para 12/11 as 08:00 na UBS Vila Mariana.', 'S');
INSERT INTO tb_lembrete VALUES (sq_tb_lembrete.NEXTVAL, 13, TO_DATE('2026-11-02 12:05', 'YYYY-MM-DD HH24:MI'), 'WHATSAPP', 'Ola, Juliana! Consulta de pre-natal agendada para 12/11 as 10:30 na UBS Mooca.', 'N');

COMMIT;


-- =====================================================================
-- 3. CONSULTAS (10)
-- =====================================================================

-- Consulta 1: Qual e a fila do dia 12/11/2026 na UBS Vila Mariana,
--             em ordem de prioridade e horario?
-- Recursos: INNER JOIN, WHERE, ORDER BY
SELECT co.nr_prioridade                         AS prioridade,
       TO_CHAR(co.dt_consulta, 'HH24:MI')       AS horario,
       ci.nm_cidadao                            AS cidadao,
       m.nm_medico                              AS medico,
       e.nm_especialidade                       AS especialidade
  FROM tb_consulta co
 INNER JOIN tb_cidadao       ci ON ci.cd_cidadao       = co.cd_cidadao
 INNER JOIN tb_medico        m  ON m.cd_medico         = co.cd_medico
 INNER JOIN tb_ubs           u  ON u.cd_ubs            = m.cd_ubs
 INNER JOIN tb_especialidade e  ON e.cd_especialidade  = co.cd_especialidade
 WHERE u.nm_ubs = 'UBS Vila Mariana'
   AND co.ds_status = 'AGENDADA'
   AND TRUNC(co.dt_consulta) = TO_DATE('2026-11-12', 'YYYY-MM-DD')
 ORDER BY co.nr_prioridade, co.dt_consulta;

-- Consulta 2: Quais cidadaos tem 60 anos ou mais (prioridade de idoso)?
-- Recursos: calculo no SELECT, WHERE, ORDER BY
SELECT nm_cidadao,
       TO_CHAR(dt_nascimento, 'DD/MM/YYYY')               AS nascimento,
       TRUNC(MONTHS_BETWEEN(SYSDATE, dt_nascimento) / 12) AS idade
  FROM tb_cidadao
 WHERE MONTHS_BETWEEN(SYSDATE, dt_nascimento) / 12 >= 60
 ORDER BY idade DESC;

-- Consulta 3: Quantas consultas existem em cada status?
-- Recursos: COUNT, GROUP BY, ORDER BY
SELECT ds_status       AS status,
       COUNT(*)        AS qtd_consultas
  FROM tb_consulta
 GROUP BY ds_status
 ORDER BY qtd_consultas DESC;

-- Consulta 4: Qual e o tempo de espera medio, maximo e minimo em cada UBS?
-- Recursos: AVG, MAX, MIN, GROUP BY, INNER JOIN, WHERE
SELECT u.nm_ubs                        AS ubs,
       ROUND(AVG(co.nr_espera_min), 1) AS espera_media_min,
       MAX(co.nr_espera_min)           AS espera_maxima_min,
       MIN(co.nr_espera_min)           AS espera_minima_min
  FROM tb_consulta co
 INNER JOIN tb_medico m ON m.cd_medico = co.cd_medico
 INNER JOIN tb_ubs    u ON u.cd_ubs    = m.cd_ubs
 WHERE co.ds_status = 'REALIZADA'
 GROUP BY u.nm_ubs
 ORDER BY espera_media_min DESC;

-- Consulta 5: Quais UBS tem mais de 2 consultas registradas?
-- Recursos: COUNT, GROUP BY, HAVING
SELECT u.nm_ubs      AS ubs,
       COUNT(*)      AS qtd_consultas
  FROM tb_consulta co
 INNER JOIN tb_medico m ON m.cd_medico = co.cd_medico
 INNER JOIN tb_ubs    u ON u.cd_ubs    = m.cd_ubs
 GROUP BY u.nm_ubs
HAVING COUNT(*) > 2
 ORDER BY qtd_consultas DESC;

-- Consulta 6: Quantos minutos de agenda cada medico tem reservados
--             em consultas ainda nao realizadas (status AGENDADA)?
-- Recursos: SUM, calculo no SELECT, GROUP BY, WHERE
SELECT m.nm_medico                           AS medico,
       COUNT(*)                              AS consultas_agendadas,
       SUM(e.nr_duracao_min)                 AS minutos_reservados,
       ROUND(SUM(e.nr_duracao_min) / 60, 2)  AS horas_reservadas
  FROM tb_consulta co
 INNER JOIN tb_medico        m ON m.cd_medico        = co.cd_medico
 INNER JOIN tb_especialidade e ON e.cd_especialidade = co.cd_especialidade
 WHERE co.ds_status = 'AGENDADA'
 GROUP BY m.nm_medico
 ORDER BY minutos_reservados DESC;

-- Consulta 7: Quais consultas tiveram tempo de espera acima da media geral?
-- Recursos: SUBCONSULTA (SUBSELECT), calculo no SELECT
SELECT co.cd_consulta,
       ci.nm_cidadao,
       co.nr_espera_min,
       co.nr_espera_min - (SELECT ROUND(AVG(nr_espera_min), 1) FROM tb_consulta)
                                            AS minutos_acima_da_media
  FROM tb_consulta co
 INNER JOIN tb_cidadao ci ON ci.cd_cidadao = co.cd_cidadao
 WHERE co.nr_espera_min > (SELECT AVG(nr_espera_min) FROM tb_consulta)
 ORDER BY co.nr_espera_min DESC;

-- Consulta 8: Quantas consultas cada cidadao ja marcou,
--             incluindo quem nunca marcou nenhuma?
-- Recursos: LEFT JOIN, COUNT, GROUP BY
SELECT ci.nm_cidadao               AS cidadao,
       COUNT(co.cd_consulta)       AS qtd_consultas
  FROM tb_cidadao ci
  LEFT JOIN tb_consulta co ON co.cd_cidadao = ci.cd_cidadao
 GROUP BY ci.nm_cidadao
 ORDER BY qtd_consultas DESC, ci.nm_cidadao;

-- Consulta 9: Quais medicos atendem cada especialidade,
--             incluindo especialidades que ainda nao tem medico?
-- Recursos: RIGHT JOIN, INNER JOIN, ORDER BY
SELECT e.nm_especialidade                      AS especialidade,
       NVL(m.nm_medico, '(sem medico)')        AS medico
  FROM tb_medico m
 INNER JOIN tb_medico_especialidade me ON me.cd_medico = m.cd_medico
 RIGHT JOIN tb_especialidade e         ON e.cd_especialidade = me.cd_especialidade
 ORDER BY e.nm_especialidade, m.nm_medico;

-- Consulta 10: Quantos lembretes foram enviados por canal
--              e quantos foram confirmados pelo cidadao?
-- Recursos: COUNT, GROUP BY, ORDER BY
SELECT ds_canal          AS canal,
       fl_confirmado     AS confirmado,
       COUNT(*)          AS qtd_lembretes
  FROM tb_lembrete
 GROUP BY ds_canal, fl_confirmado
 ORDER BY ds_canal, fl_confirmado;


-- =====================================================================
-- 4. UPDATE (com WHERE)
-- A responsavel pelo Lucas cancelou pelo chatbot a consulta de Pediatria de 10/11.
-- =====================================================================
UPDATE tb_consulta
   SET ds_status = 'CANCELADA'
 WHERE cd_consulta = 11;

-- Conferencia do UPDATE
SELECT cd_consulta, ds_status FROM tb_consulta WHERE cd_consulta = 11;


-- =====================================================================
-- 5. DELETE (com WHERE)
-- Remove os lembretes de consultas canceladas, que nao precisam mais ser enviados.
-- =====================================================================
DELETE FROM tb_lembrete
 WHERE cd_consulta IN (SELECT cd_consulta
                         FROM tb_consulta
                        WHERE ds_status = 'CANCELADA');

-- Conferencia do DELETE (deve mostrar 6 lembretes)
SELECT cd_lembrete, cd_consulta FROM tb_lembrete ORDER BY cd_lembrete;

COMMIT;
