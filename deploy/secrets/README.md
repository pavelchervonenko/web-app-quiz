# RSA Keys

В этой папке локально хранятся RSA-ключи для JWT.
Сами `.pem` файлы нельзя коммитить в git.

Сгенерировать ключи:

```bash
mkdir -p deploy/secrets

openssl genpkey \
  -algorithm RSA \
  -out deploy/secrets/jwt-private.pem \
  -pkeyopt rsa_keygen_bits:2048

openssl rsa \
  -in deploy/secrets/jwt-private.pem \
  -pubout \
  -out deploy/secrets/jwt-public.pem

chmod 600 deploy/secrets/jwt-private.pem
chmod 644 deploy/secrets/jwt-public.pem
```

Пути к этим файлам указываются в `.env`:

```env
JWT_PUBLIC_KEY_PATH=./deploy/secrets/jwt-public.pem
JWT_PRIVATE_KEY_PATH=./deploy/secrets/jwt-private.pem
```

Spring Boot внутри контейнера читает их через:

```env
RSA_PUBLIC_KEY=file:/run/secrets/jwt-public.pem
RSA_PRIVATE_KEY=file:/run/secrets/jwt-private.pem
```
