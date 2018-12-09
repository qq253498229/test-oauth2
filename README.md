# Spring 认证服务器

- [获取code](#获取code)
- [通过code获取token](#通过code获取token)
- [检查token](#检查token)
- [刷新token](#刷新token)
- [注销](#注销)

## 获取code

`GET` 请求:

```
http://localhost:12222/oauth/authorize?response_type=code&client_id=client&redirect_uri=http%3A%2F%2Fwww.baidu.com
```

登录成功后跳转到URL:

```
https://www.baidu.com/?code=annqpj
```

## 通过code获取token

`POST` 请求(其中Authorization为client:secret的base64加密):

```
curl -X POST \
  http://localhost:12222/oauth/token \
  -H 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=authorization_code&redirect_uri=http%3A%2F%2Fwww.baidu.com&code=annqpj'
```

成功后返回:

```json
{
    "access_token": "50d421f1-5326-46d6-985a-24cdea6ad4b4",
    "token_type": "bearer",
    "refresh_token": "6fc140f2-6cec-4d72-91ec-724fae528245",
    "expires_in": 43199,
    "scope": "app"
}
```

code错误返回:

```json
{
    "error": "invalid_grant",
    "error_description": "Invalid authorization code: annqpj123"
}
```

## 检查token

`GET` 请求(其中Authorization为client:secret的base64加密):

```
curl -X GET \
  'http://localhost:12222/oauth/check_token?token=50d421f1-5326-46d6-985a-24cdea6ad4b4' \
  -H 'Authorization: Basic Y2xpZW50OnNlY3JldA=='
```

成功后返回:

```json
{
    "active": true,
    "exp": 1544376932,
    "user_name": "user",
    "authorities": [
        "ROLE_USER"
    ],
    "client_id": "client",
    "scope": [
        "app"
    ]
}
```

token错误返回:

```json
{
    "error": "invalid_token",
    "error_description": "Token was not recognised"
}
```

## 刷新token

`POST` 请求:

```
curl -X POST \
  http://localhost:12222/oauth/token \
  -H 'Authorization: Basic Y2xpZW50OnNlY3JldA==' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=refresh_token&refresh_token=6fc140f2-6cec-4d72-91ec-724fae528245'
```

成功后返回:

```json
{
    "access_token": "7b784f5f-96fa-4f3e-9440-be68b43292ba",
    "token_type": "bearer",
    "refresh_token": "6fc140f2-6cec-4d72-91ec-724fae528245",
    "expires_in": 43199,
    "scope": "app"
}
```

refresh_token错误返回:

```json
{
    "error": "invalid_grant",
    "error_description": "Invalid refresh token: 6fc140f2-6cec-4d72-91ec-724fae528245123"
}
```

## 注销

`GET` 请求:

```
http://localhost:12222/logout
```