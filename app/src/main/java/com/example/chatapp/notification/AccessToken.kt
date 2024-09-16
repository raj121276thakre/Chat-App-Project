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
                    "  \"project_id\": \"chat-app-56add\",\n" +
                    "  \"private_key_id\": \"77fdd636df01d0d33961264253c68293d2c697bb\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCmvyXUa60NceoX\\n3m3gX8xRMFprJuZMbsYWO5rXf3gA0JrOLOSFJSewvO6CeL/pPF6BfHtbKMstin+w\\nCRyEnuBw0EBXbXT8E77DRkYn1gvn6c48S9HqF/c7lnfzghGTcVi1dG5Lw7SpqEYs\\nTjkQSfZr09kxSCn/k6Mnyu4hJvnEkH3qZwfGvrMenXxuNsaMQBOg4FT5r8NnTYZ8\\nlpd/W1GBpK/woXQgD2BTc8VIk5lX9gYhyYgOtU8X8gQ3k5NN6CDvvszX2izSLHB/\\nZJZe6r0mYOw+g8qhOor6K+TTabnDcP0/UF53CfH7aghlfw6EaCx1RF/99W16Y4Ar\\nFEwmFvrBAgMBAAECggEAKbv/6OnGV24ijDTUxWrUxaFXkCgdThYPqDLk73js/S84\\n/8+GpriE9ZxkCe3VArhkYv5YFZnhOJXInbO6Got1W2cUI4220PzUKyXkm0XclwAS\\n6U6EzJWRAK4Jeb4dTgz5ifkJPhdOmYQPhDlT2PraHWTZoJcSQ/CcznHhJhUOQJ8N\\npveME0dokRhWGh1KddqM9W4NSt6PXguD6nLcxZTM7iIIc37w9cdbCfPsVt/MfbJM\\nZMs5IuNAvEMipIPxXMhzdbh2snHzewpLFhSj8vG8/euKL83aDuY7AwvXKDkhPpRX\\n8H6nZe8dbEYAKaF2J16s7u2ymM9u5Y9Lsp3y/e3MzQKBgQDkEMDiCFWHm+2++92g\\nNSuYH9TKbvDaYUgHyfmOpjDoDMQiGuc89GJVSJDq+/Io04sTGHSf0twntoyEHrHw\\nTu5ZpG95ZjfrAHft/a2EQLu8L+ahe4enOzjetQ844Fffxgzev72wJ6ngUxvHdew1\\nZj8srslRnQpbALFxUgyBMYN5nwKBgQC7K6z/inKpT1NgFEOCdybqyBZobTBGa9+6\\nESHciIwObCtJT/RcFdZDiOGZgK1vfr16LNfloC3PwVmIB+xUT26joTYhQOYh9zJI\\nqWRii/eppcQgai5ZCxODVCjLiXAQJnmSDeoMzPi66IF/+lUoS1RFt00J8DRp38b7\\nmxeX1D3vnwKBgQCrET38zY+KJpXQOY/lybwnYUj1hv22cF3dKcy9CT+J73l2s4Nb\\nn1va6JF7EPx1Tyf0bNWdxEZKq4z+1R9XR31VVBghkWDELxfGNg/fMccfwzZVThlU\\ngQGJW3pNiUmgmiexNVWNOlMb5nrhxDwnWchtNbXgN9E6rWxrucWTapZZtQKBgQCg\\na/fT8msCWJ4ibFvMd6mgc6xZBrTots7D4KlAJry8I9QZ+vB6+LBzo9+QvKxqMpve\\nGo9VHawGNDczk27oeEhg8Oy/JavZktDDXIRQvpygCBcInVlqMkcgOcLZaMvm28M4\\n/roLDKdnM57C/OF+LsplEo1yJjIWSJl4z14yYNRRCwKBgGaaR+ClgOwvuPog21dn\\nXIhz4cAOvjd5ZQFjOYinGUS482sfjyANEVXEaa9+C1WyI384F2phzwN3KafSKWB7\\nYUgXCUhuD9R884NyTafv3NPXQxMtn88p69jYfdyNDZcrL8kIXoVZS0vnkVmoPtCx\\nw82L0SWqW0uY7plIQhfsQpuf\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-1k116@chat-app-56add.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"106274206718558130622\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-1k116%40chat-app-56add.iam.gserviceaccount.com\",\n" +
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
