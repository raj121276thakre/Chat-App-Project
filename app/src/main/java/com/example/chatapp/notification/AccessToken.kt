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
                    "  \"project_id\": \"chat-app-2025-a5acd\",\n" +
                    "  \"private_key_id\": \"fa67db71cb4efce11832560967e34232fe76b92f\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCUOCKkO0SMKr70\\nvISXqkNxMNfmRObaCJ/hPZARDCSvU+ExKB7WQc8WjeJnG8Dob1fhgqir20Y6kIp/\\nXs9iQUXSXBPsIPLmkQ8nSZQUQjZDB850VNjhmblW6UxezUfcOcdOSTZ4L9L6BdLu\\nO8xUqjQY1c9wJKA7pSNQlmUWe6gUH30giVwd4zbpyXDNf7RsxeWavD/vKDKiONAR\\nDyS15PSKLqZdjsyv9DfiYQb+4ukaEOJ2kF/QlIH2WbI6/H2QZiPX3a4MyhEZBmMj\\nGRLi8YxgSdMIu9shRPLozA4iN98o45He/MnpmPnwxPEYD3Vuksf85P64EES+y05M\\nX37w0NHrAgMBAAECggEAD4CRivjR8hEUeNdwzMYXmLKhmilxtbM6OZ4tP8Mz4QyW\\nXIRUOVeXt8WuPNeHxSfEGseGKbdc8IommizJ5v5ptZdA1C0cCqZ4XcUR0k6ci0U5\\nQCyRpILmAiOTg8TXs75tTJBWvJpP6CuMxLMHa4GDwmc2c4W5JpPejjK0rvXO/g+m\\nRsmqGUpVaexLidH7PCpK7CXBRaXyJk2L8q0/vuQaQx9qr90yWeqtMv+a1DXyPPjZ\\npl0+/PaTTlxB8FbYx75PAuhRfR0B8SbbSaBG1HtVFhjed8H+cj60svMhhnD0nYUk\\nVwzPkI1ptZ66EfKVGC8dycrTiklm7jXUHKQB8AzorQKBgQDLjb5jTF9V3UieFp7w\\nALn30JBgFVo7yS4GfU6LSOiOhRUn1AlQJzENtA1gI4D9oIYbalpoeKF9HnodGe5O\\n56W7Dq2647b3X8MD1REka/N7ijp/gd7VMVpnsBBMRFYDIxppFMfoPsKs2MlEwcJP\\nZFQZI3qI6tn3tyg8soVmyv+YBQKBgQC6aJLOIbQIB8CTveYMxx41EfJ0jeyr8Ap/\\neI5Lec1HVJGhX0ROf7kh8HLrFFifRRCzsY2W52ZIWgcfYwT9gZbU79RzqGltfRp/\\nWYzkHB6ORWnt765jDggM9jk1kToN06MIU9iSg5EazcHCKQyES7Uh4jnD1eECXhZZ\\nYmxmsBWVLwKBgCZW9F1PrUVHAL6GFDmwaVa7+fWfD/fi1MB0Ka8idGsjtfBih76J\\nIzwphbtt9IXRDnFrfsverMwcSXPhxnkmOJkbEq345iWB7AF0yF3L13iVeJ87nvUY\\n7qZJC0jSNkExA3B3cY/VAmX8DyzcMfUyOITjodjYI2X9IZ8NIQNTiQDhAoGAUG9F\\nQYsLexeVQmsDtGnLIA1BeIexIzPt47kqdWj2ahwUxvQrrk+wm/N8MvHRl9Aed9ta\\nhE4PNZMbVbDNe3IKmpFuNAXskCEWaPNENz3m9W8AWXo93mI0mRs57mJfEY5L4BEF\\n95qr50v3modJ7mAYpalPUbCdaf8dV65/pgO/HGMCgYEAkPPNpKaBgjGF/JrNciMv\\n+jshNIAqLLOKLx+12QWKggUnm0hsunLErhek3lMP277ccuylBII5qWFeAJ3kWGcO\\nVDwqtFX3dsVxbevcCoFGNuldFfPRKIfnxE43O9sB5SNtrFmUrtLAVIVbWLz19vPa\\nLaRc3WhuXnHFHxxKsIhDz3E=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-fbsvc@chat-app-2025-a5acd.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"103536471159104247577\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-fbsvc%40chat-app-2025-a5acd.iam.gserviceaccount.com\",\n" +
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


//val jsonString = "{\n" +
//        "  \"type\": \"service_account\",\n" +
//        "  \"project_id\": \"chat-app-56add\",\n" +
//        "  \"private_key_id\": \"77fdd636df01d0d33961264253c68293d2c697bb\",\n" +
//        "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCmvyXUa60NceoX\\n3m3gX8xRMFprJuZMbsYWO5rXf3gA0JrOLOSFJSewvO6CeL/pPF6BfHtbKMstin+w\\nCRyEnuBw0EBXbXT8E77DRkYn1gvn6c48S9HqF/c7lnfzghGTcVi1dG5Lw7SpqEYs\\nTjkQSfZr09kxSCn/k6Mnyu4hJvnEkH3qZwfGvrMenXxuNsaMQBOg4FT5r8NnTYZ8\\nlpd/W1GBpK/woXQgD2BTc8VIk5lX9gYhyYgOtU8X8gQ3k5NN6CDvvszX2izSLHB/\\nZJZe6r0mYOw+g8qhOor6K+TTabnDcP0/UF53CfH7aghlfw6EaCx1RF/99W16Y4Ar\\nFEwmFvrBAgMBAAECggEAKbv/6OnGV24ijDTUxWrUxaFXkCgdThYPqDLk73js/S84\\n/8+GpriE9ZxkCe3VArhkYv5YFZnhOJXInbO6Got1W2cUI4220PzUKyXkm0XclwAS\\n6U6EzJWRAK4Jeb4dTgz5ifkJPhdOmYQPhDlT2PraHWTZoJcSQ/CcznHhJhUOQJ8N\\npveME0dokRhWGh1KddqM9W4NSt6PXguD6nLcxZTM7iIIc37w9cdbCfPsVt/MfbJM\\nZMs5IuNAvEMipIPxXMhzdbh2snHzewpLFhSj8vG8/euKL83aDuY7AwvXKDkhPpRX\\n8H6nZe8dbEYAKaF2J16s7u2ymM9u5Y9Lsp3y/e3MzQKBgQDkEMDiCFWHm+2++92g\\nNSuYH9TKbvDaYUgHyfmOpjDoDMQiGuc89GJVSJDq+/Io04sTGHSf0twntoyEHrHw\\nTu5ZpG95ZjfrAHft/a2EQLu8L+ahe4enOzjetQ844Fffxgzev72wJ6ngUxvHdew1\\nZj8srslRnQpbALFxUgyBMYN5nwKBgQC7K6z/inKpT1NgFEOCdybqyBZobTBGa9+6\\nESHciIwObCtJT/RcFdZDiOGZgK1vfr16LNfloC3PwVmIB+xUT26joTYhQOYh9zJI\\nqWRii/eppcQgai5ZCxODVCjLiXAQJnmSDeoMzPi66IF/+lUoS1RFt00J8DRp38b7\\nmxeX1D3vnwKBgQCrET38zY+KJpXQOY/lybwnYUj1hv22cF3dKcy9CT+J73l2s4Nb\\nn1va6JF7EPx1Tyf0bNWdxEZKq4z+1R9XR31VVBghkWDELxfGNg/fMccfwzZVThlU\\ngQGJW3pNiUmgmiexNVWNOlMb5nrhxDwnWchtNbXgN9E6rWxrucWTapZZtQKBgQCg\\na/fT8msCWJ4ibFvMd6mgc6xZBrTots7D4KlAJry8I9QZ+vB6+LBzo9+QvKxqMpve\\nGo9VHawGNDczk27oeEhg8Oy/JavZktDDXIRQvpygCBcInVlqMkcgOcLZaMvm28M4\\n/roLDKdnM57C/OF+LsplEo1yJjIWSJl4z14yYNRRCwKBgGaaR+ClgOwvuPog21dn\\nXIhz4cAOvjd5ZQFjOYinGUS482sfjyANEVXEaa9+C1WyI384F2phzwN3KafSKWB7\\nYUgXCUhuD9R884NyTafv3NPXQxMtn88p69jYfdyNDZcrL8kIXoVZS0vnkVmoPtCx\\nw82L0SWqW0uY7plIQhfsQpuf\\n-----END PRIVATE KEY-----\\n\",\n" +
//        "  \"client_email\": \"firebase-adminsdk-1k116@chat-app-56add.iam.gserviceaccount.com\",\n" +
//        "  \"client_id\": \"106274206718558130622\",\n" +
//        "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
//        "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
//        "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
//        "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-1k116%40chat-app-56add.iam.gserviceaccount.com\",\n" +
//        "  \"universe_domain\": \"googleapis.com\"\n" +
//        "}\n"