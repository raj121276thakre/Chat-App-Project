package com.example.chatapp.notification

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessToken {

    // Firebase Messaging Scope URL
    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"


    fun getAccessToken(): String? {

        try {
            val jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"chat-app-15577\",\n" +
                    "  \"private_key_id\": \"87647c95f12e295f7e0e5020a034352f0f0277af\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDNZ91xaRiS6Bwc\\nITin1b/lEZTTrEAwwFMRkDUUN/KItrtip1TpbzscBMEOF7Je8PCp1o5l2B79UELL\\n6vBhviXMlummKg1UEc8JfO4M6JQtLVhREeAH7F5zJGLBCyO2qbASc2NVXMkmdYEA\\nppcqhKUqYRw3783zkRvNIvRweMJn6yCxaPiNe8Lmqazf3XC/N0azHHwnVp9kjA+0\\niIRmmalN9YZgnoWoMRXCvWFKKFKDekFy4lPEmErr6rQZGepWiePyqLsk57Fcmokw\\ng6RdfgbSJLRdWoajpU+JHnJxGsmdc7O6Xtw4ZB13Y/VpFZiOSwN8TR0ozbzh4dzQ\\nS6Ma20c7AgMBAAECggEAAuWCOb1bKNstZkzQ9ouXmKcuUOvUEzvrD/YszBuxMlyt\\nTh9WSF0WrsxMXU1X6RR/DaNmalOUCvYXnbYy7JXuBNwN8GzAYl1Gl6Io3eAREtqv\\nAk79De+Mh2rNxQjKA03nmzhVqsL+XSn5XI/SKuR9oG2Hzywfsd3BJXE8Pt0dFLJg\\nHp1+zlzV8itG1NKrFyiv0V4N4Nzf9vb1bbLxq1pTq40EyIDxlEHThsuulYsNPDfF\\nTLJ9cGCI+0o95f1wcL8W9LFwjXcCNE2DSh7BT2fLHMpAWRHyFrhUVuOFme/6g2lx\\nwKyB63iiJnAaaonoa1S0eDKKXBz352qSkPgXeMbLbQKBgQD91vKksJX3NNao87us\\nIcwBQGdmvstJRGaK8Clue5vgX0rtyiCEebXKPz2ms21MhD6a+cRk6uHf1MkAOQna\\nfEHnrRDbgvUQ6NyXL1IvPLEQbaYK+VwxgGv9atoKtrIMpPKW+Rm3OZ+0X5jB7zkS\\ng6aob4mTfJUDlwH989UqvTddpwKBgQDPJ2RYDqxesPpNzy9RtIS3VC4FaUJM+46I\\nmfqFytwTexo6uQJMYRuOj8SZJayiYn3xOj+k1ZCNw7yugHK92oDyUEl1eSoqdBjq\\nlLucU4KgotAQNZsezGez7vADar9om0Hc9u/waoIIrMyUktc52EjoErd43w28pgQd\\n9f71VQaETQKBgC9Pu+P0Qc9Umtncq9MWbYaiMWOWFh+uzwoAX0iHY3M7b6TIcq0e\\nXkK27M/AS5PiZiTOfstsd/MXHuuatfpyBHqg6C5xxymCrXLEUCYbwsjUFwmelo9n\\nZzlRjBhJGwB+UfTPsvpSlDCzTK6A3vtTbGGigFrr8n47zBRAOTABGmurAoGBAK8d\\nhLTvyoTNpPqrvzuZl2uGzNSkM3jyWn5ddCsv3rTVJsonZar9mBb+r9oqv/IRwpdu\\nfyivpOBCbDkzukuF7+VnO4ID6EI1HG8JRkOTUOTMXYimWDhD4RJ5VQn+bkpWnOnX\\nEeQxlTUSJmsearSqgo80a0bRgEJOMULGlj38H2MtAoGBALC9haMjVYq9VLvNyNLm\\nxTOkRirKqWkBqI3v2L7nGnYs8pdiebpTCDIxh+oggsRzoFjmKPKC1/k8mowJY0Uz\\nwLjQjbugT86hKtj/1AOAlf2B8mNo+fzFBOEmE8xRwnG3Ox+v92wIz+wUNNlT/U9Y\\nhVLvj2yscTaz9dX60eLheoGJ\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-98uzs@chat-app-15577.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"102315896469228807801\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-98uzs%40chat-app-15577.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n"

            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))

            val googleCredentials = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf(firebaseMessagingScope))
            googleCredentials.refreshIfExpired()

            return googleCredentials.accessToken.tokenValue
        } catch (e: IOException) {

            return null

        }


    }


}
