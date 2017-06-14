--GP-523

CREATE SEQUENCE seq_tracciati start 1 increment 1 maxvalue 9223372036854775807 minvalue 1 cache 1 NO CYCLE;

CREATE TABLE tracciati
(
       data_caricamento TIMESTAMP NOT NULL,
       data_ultimo_aggiornamento TIMESTAMP NOT NULL,
       stato VARCHAR(255) NOT NULL,
       linea_elaborazione BIGINT NOT NULL,
       descrizione_stato VARCHAR(1024),
       num_linee_totali BIGINT NOT NULL,
       num_operazioni_ok BIGINT NOT NULL,
       num_operazioni_ko BIGINT NOT NULL,
       nome_file VARCHAR(255) NOT NULL,
       raw_data_richiesta BYTEA NOT NULL,
       raw_data_risposta BYTEA,
       -- fk/pk columns
       id BIGINT DEFAULT nextval('seq_tracciati') NOT NULL,
       id_operatore BIGINT NOT NULL,
       -- check constraints
       CONSTRAINT chk_tracciati_1 CHECK (stato IN ('ANNULLATO','NUOVO','IN_CARICAMENTO','CARICAMENTO_OK','CARICAMENTO_KO')),
       -- fk/pk keys constraints
       CONSTRAINT fk_tracciati_1 FOREIGN KEY (id_operatore) REFERENCES operatori(id),
       CONSTRAINT pk_tracciati PRIMARY KEY (id)
);




CREATE SEQUENCE seq_operazioni start 1 increment 1 maxvalue 9223372036854775807 minvalue 1 cache 1 NO CYCLE;

CREATE TABLE operazioni
(
       tipo_operazione VARCHAR(255) NOT NULL,
       linea_elaborazione BIGINT NOT NULL,
       stato VARCHAR(255) NOT NULL,
       dati_richiesta BYTEA NOT NULL,
       dati_risposta BYTEA,
       dettaglio_esito VARCHAR(255),
       cod_versamento_ente VARCHAR(255),
       -- fk/pk columns
       id BIGINT DEFAULT nextval('seq_operazioni') NOT NULL,
       id_tracciato BIGINT NOT NULL,
       id_applicazione BIGINT,
       -- check constraints
       CONSTRAINT chk_operazioni_1 CHECK (stato IN ('NON_VALIDO','ESEGUITO_OK','ESEGUITO_KO')),
       -- fk/pk keys constraints
       CONSTRAINT fk_operazioni_1 FOREIGN KEY (id_tracciato) REFERENCES tracciati(id),
       CONSTRAINT fk_operazioni_2 FOREIGN KEY (id_applicazione) REFERENCES applicazioni(id),
       CONSTRAINT pk_operazioni PRIMARY KEY (id)
);


