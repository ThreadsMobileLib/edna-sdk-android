package io.edna.threads.demo.snapshot

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.edna.threads.demo.R
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ChatTextSnapshotTest3 : SnapshotBaseTest(R.raw.snapshot_test_history_text_response_3) {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val mockWebHTMLServer = MockWebServer()
    private val mockWebImageServer = MockWebServer()

    private val mockedHtml = "<!DOCTYPE html>\n" +
        "\n" +
        "<html lang=\"ru-RU\" prefix=\"og: https://ogp.me/ns#\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" >\n" +
        "    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\">\n" +
        "    <link href=\"https://fonts.googleapis.com/css2?family=Noto+Sans:ital,wght@0,400;0,700;1,400;1,700&display=swap\" rel=\"stylesheet\">\n" +
        "    <link rel=\"shortcut icon\" href=\"/favicon.ico\" type=\"image/x-icon\">\n" +
        "    <link rel=\"icon\" href=\"/favicon.ico\" type=\"image/x-icon\">\n" +
        "    <meta name=\"facebook-domain-verification\" content=\"khkz8sbjw409pv5919ehnhbs8rzi03\" />\n" +
        "    <script type=\"text/javascript\"\n" +
        "\t\tid=\"Cookiebot\"\n" +
        "\t\tsrc=\"https://consent.cookiebot.com/uc.js\"\n" +
        "\t\tdata-cbid=\"fb7a4fa2-f601-4b82-87b5-677978ffb314\"\n" +
        "\t\t\t\t\t\tdata-culture=\"RU\"\n" +
        "\t\t\t\t\t></script>\n" +
        "\n" +
        "\t\t<!-- Meta Tag Manager -->\n" +
        "\t\t<meta name=\"keywords\" content=\"mfms, mfms ru, mfms whatsapp, mfms отзывы, mfms компания, online mfms ru, мфмс, mfm solutions, мфм солюшенс, мфм солюшнс, edna, эдна, эдна компания, омниканальные коммуникации, цифровые коммуникации, коммуникационные шлюзы, коммуникационная платформа, автоматизация бизнеса, автоматизация бизнес процессов, коммуникация с клиентом, уведомление клиенту, информирование клиентов, подключить аутентификацию, push уведомление, оповещение клиентов, смс оповещение клиентов, cdp, customer data platform\" />\n" +
        "\t\t<!-- / Meta Tag Manager -->\n" +
        "\n" +
        "<!-- SEO от Rank Math - https://s.rankmath.com/home -->\n" +
        "<meta name=\"description\" content=\"edna (ранее MFMS) - лидер в сфере IT-решений для бизнеса в сфере неголосовых коммуникаций с 2005 года. Подключение и интеграция всех видов мессенджеров и цифровых каналов. Облачный чат-центр. Автоматизация и повышение эффективности бизнес-процессов. Официальный партнер Apple, Facebook, Viber, Vk-notify.\"/>\n" +
        "<meta name=\"robots\" content=\"follow, index, max-snippet:-1, max-video-preview:-1, max-image-preview:large\"/>\n" +
        "<link rel=\"canonical\" href=\"https://edna.ru/\" />\n" +
        "<meta property=\"og:locale\" content=\"ru_RU\" />\n" +
        "<meta property=\"og:type\" content=\"website\" />\n" +
        "<meta property=\"og:title\" content=\"edna - омниканальные коммуникации для работы с клиентами в цифровых каналах коммуникации\" />\n" +
        "<meta property=\"og:description\" content=\"edna (ранее MFMS) - лидер в сфере IT-решений для бизнеса в сфере неголосовых коммуникаций с 2005 года. Подключение и интеграция всех видов мессенджеров и цифровых каналов. Облачный чат-центр. Автоматизация и повышение эффективности бизнес-процессов. Официальный партнер Apple, Facebook, Viber, Vk-notify.\" />\n" +
        "<meta property=\"og:url\" content=\"https://edna.ru/\" />\n" +
        "<meta property=\"og:site_name\" content=\"edna\" />\n" +
        "<meta property=\"og:updated_time\" content=\"2022-03-21T12:01:34+03:00\" />\n" +
        "<meta property=\"og:image\" content=\"http://127.0.0.1:8090/test_image2.jpg\" />\n" +
        "<meta property=\"og:image:secure_url\" content=\"http://127.0.0.1:8090/test_image2.jpg\" />\n" +
        "<meta property=\"og:image:width\" content=\"820\" />\n" +
        "<meta property=\"og:image:height\" content=\"312\" />\n" +
        "<meta property=\"og:image:alt\" content=\"edna (ранее mfms)\" />\n" +
        "<meta property=\"og:image:type\" content=\"image/jpeg\" />\n" +
        "</head>\n" +
        "<body class=\"home page-template page-template-templates page-template-template-fill-width page-template-templatestemplate-fill-width-php page page-id-2\">\n" +
        "\n" +
        "\t</body>\n" +
        "</html>\n"

    init {
        mockWebHTMLServer.enqueue(MockResponse().setBody(mockedHtml))
        mockWebImageServer.enqueue(MockResponse().setBody(getBinaryFileAsBuffer(getFileFromAssets("test_image2.jpg"))))
        mockWebHTMLServer.start(8080)
        mockWebImageServer.start(8090)
        mockWebHTMLServer.url("/channels")
        mockWebImageServer.url("/test_image2.jpg")
    }

    @After
    fun after() {
        mockWebHTMLServer.shutdown()
        mockWebImageServer.shutdown()
    }

    private fun getFileFromAssets(fileName: String): File = File(context.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open("test_files/$fileName").use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }

    private fun getBinaryFileAsBuffer(file: File) = Buffer().write(file.readBytes())
}
