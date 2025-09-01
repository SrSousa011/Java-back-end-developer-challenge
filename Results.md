# Resultados das Etapas - Desafio Java Back-end

## Etapa 1 - Resumo Criptográfico SHA-512

### Arquivo Processado
- **Nome:** doc.txt
- **Conteúdo:** "Teste vaga back-end Java"
- **Tamanho:** 24 bytes

### Resultado Hash SHA-512
```
dc1a7de77c59a29f366a4b154b03ad7d99013e36e08beb50d976358bea7b045884fe72111b27cf7d6302916b2691ac7696c1637e1ab44584d8d6613825149e35
```

### Resposta da API (/api/hash)
```json
{
  "filename": "doc.txt",
  "hash": "dc1a7de77c59a29f366a4b154b03ad7d99013e36e08beb50d976358bea7b045884fe72111b27cf7d6302916b2691ac7696c1637e1ab44584d8d6613825149e35",
  "algorithm": "SHA-512"
}
```

---

## Etapa 2 - Assinatura Digital CMS

### Parâmetros de Assinatura
- **Arquivo:** doc.txt
- **Algoritmo de Hash:** SHA-512
- **Algoritmo de Criptografia:** RSA
- **Padrão:** CMS (Cryptographic Message Syntax) Attached
- **Certificado:** certificado_teste_hub (PKCS#12)
- **Alias:** e2618a8b-20de-4dd2-b209-70912e3177f4

### Resultado da Assinatura
- **Arquivo Gerado:** doc.txt.p7s
- **Formato:** Base64 (CMS/PKCS#7)
- **Tamanho da Assinatura:** 3,484 bytes (Base64)

### Resposta da API (/api/signature)
```json
{
  "p7sFile": "doc.txt.p7s",
  "filename": "doc.txt",
  "signature": "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",
  "fileSize": "24",
  "downloadP7s": "C:\\Users\\lucas\\OneDrive\\Documentos\\Java-back-end-developer-challenge\\src\\main\\resources\\signatured\\doc.txt.p7s",
  "p7sSaved": "true",
  "algorithm": "SHA-512 with RSA (CMS attached)"
}
```

---

## Etapa 3 e 4 - Verificação de Assinatura via API

### Processo de Verificação
- **Arquivo Verificado:** doc.txt.p7s
- **Método:** POST /api/verify
- **Validação Criptográfica:** VÁLIDA
- **Validação da Cadeia de Confiança:** Verificada contra certificados em /cadeia/

### Informações Extraídas do Signatário

#### Certificado 1: ac_bry_servidor_seguro_v3.cer
- **Nome do Arquivo:** ac_bry_servidor_seguro_v3.cer
- **Hash do Documento:** 6DB476A3C01236AD135A62C01BF851DAE8C950B33B8801F6DAC2B6728310B450
- **Data/Hora da Assinatura:** 2025-08-30T14:12:29Z
- **Algoritmo de Hash:** SHA-512
- **Emissor:** CN=Autoridade Certificadora Raiz BRy Tecnologia v3,OU=Infraestrutura de Chaves Publicas BRy Tecnologia,O=BRy Tecnologia SA,C=BR

#### Certificado 2: ac_raiz_bry_v3.cer
- **Nome do Arquivo:** ac_raiz_bry_v3.cer
- **Hash do Documento:** 7B6F659B6EA0BCA18CF1556203A1C9AA5373800427C9B07A72EC2879EF08BC1D
- **Data/Hora da Assinatura:** 2025-08-30T14:12:29Z
- **Algoritmo de Hash:** SHA-512
- **Emissor:** CN=Autoridade Certificadora Raiz BRy Tecnologia v3,OU=Infraestrutura de Chaves Publicas BRy Tecnologia,O=BRy Tecnologia SA,C=BR

### Resposta Completa da API (/api/verify)
```json
{
  "filename": "doc.txt.p7s",
  "signingTime": "30/08/2025",
  "infos": [
    {
      "fileName": "ac_bry_servidor_seguro_v3.cer",
      "documentHash": "6DB476A3C01236AD135A62C01BF851DAE8C950B33B8801F6DAC2B6728310B450",
      "signingTime": "2025-08-30T14:12:29Z",
      "hashAlgorithm": "SHA-512",
      "signerName": "CN=Autoridade Certificadora Raiz BRy Tecnologia v3,OU=Infraestrutura de Chaves Publicas BRy Tecnologia,O=BRy Tecnologia SA,C=BR"
    },
    {
      "fileName": "ac_raiz_bry_v3.cer",
      "documentHash": "7B6F659B6EA0BCA18CF1556203A1C9AA5373800427C9B07A72EC2879EF08BC1D",
      "signingTime": "2025-08-30T14:12:29Z",
      "hashAlgorithm": "SHA-512",
      "signerName": "CN=Autoridade Certificadora Raiz BRy Tecnologia v3,OU=Infraestrutura de Chaves Publicas BRy Tecnologia,O=BRy Tecnologia SA,C=BR"
    }
  ],
  "status": "VALIDO"
}
```

---

## Etapa 4 - API REST Completa

### Endpoints Implementados

#### 1. GET /api/status
**Descrição:** Verificar status da API
**Resposta:**
```json
{
  "status": "Signature API is running",
  "description": "This API allows generating and verifying digital signatures using SHA-512 and RSA.",
  "javaVersion": "1.8.0_XXX"
}
```

#### 2. POST /api/hash
**Descrição:** Gerar hash SHA-512 de um arquivo
**Entrada:** Arquivo (multipart/form-data)
**Resposta:** Hash SHA-512 em hexadecimal

#### 3. POST /api/signature
**Descrição:** Gerar assinatura digital CMS
**Entrada:**
- Arquivo a ser assinado
- Arquivo PKCS#12 (certificado + chave privada)
- Senha do PKCS#12
  **Resposta:** Assinatura em Base64 + metadados

#### 4. POST /api/verify
**Descrição:** Verificar assinatura digital
**Entrada:**
- Arquivo assinado (.p7s)
- Arquivo original
  **Resposta:** Status de validação + informações do signatário

#### 5. GET /api/info
**Descrição:** Informações sobre algoritmos suportados
**Resposta:**
```json
{
  "hashAlgorithm": "SHA-512",
  "encryptionAlgorithm": "RSA",
  "signatureStandard": "CMS (Cryptographic Message Syntax)",
  "signatureType": "Attached",
  "javaCompatibility": "Java 8+",
  "bouncyCastleVersion": "1.70",
  "springBootVersion": "2.7.x",
  "javaEnvironment": {
    "version": "1.8.0_XXX",
    "vendor": "Oracle Corporation",
    "runtime": "1.8.0_XXX-bXX"
  }
}
```

---

## Resumo dos Resultados

### ✅ Etapa 1 - CONCLUÍDA
- Hash SHA-512 gerado com sucesso
- Valor confirmado e validado

### ✅ Etapa 2 - CONCLUÍDA
- Assinatura CMS attached gerada
- Arquivo .p7s criado e salvo
- Algoritmo SHA-512 com RSA implementado

### ✅ Etapa 3 - CONCLUÍDA
- Verificação criptográfica funcionando
- Validação de cadeia de confiança implementada
- Extração de metadados do signatário

### ✅ Etapa 4 - CONCLUÍDA
- API REST funcional com 5 endpoints
- Documentação Swagger implementada
- Tratamento de erros robusto
