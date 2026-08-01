INSERT INTO users (full_name, cpf_cnpj, email, password, user_type)
VALUES ('João Cliente', '111.111.111-11', 'joao@email.com', 'senha123', 'CLIENTE');

INSERT INTO users (full_name, cpf_cnpj, email, password, user_type)
VALUES ('Maria Lojista', '22.222.222/0001-22', 'maria@email.com', 'senha123', 'LOJISTA');

INSERT INTO wallets (user_id, balance) VALUES (1, 500.00);

INSERT INTO wallets (user_id, balance) VALUES (2, 0.00);