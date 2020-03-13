package vng.zalo.tdtai.zalo.ui.home.diary

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.model.post.Post
import vng.zalo.tdtai.zalo.model.story.ImageStory
import vng.zalo.tdtai.zalo.model.story.Story
import vng.zalo.tdtai.zalo.repo.Database
import javax.inject.Inject

class DiaryViewModel @Inject constructor(
        private val database:Database
):ViewModel() {
    val livePosts = MutableLiveData(ArrayList<Post>())
    val liveStories = MutableLiveData(ArrayList<Story>())

    init {
        var now = System.currentTimeMillis()

        val post = Post(
                "1",
                now++,
                "0123456789",
                "1 to 9 bro",
                "https://www.pngfind.com/pngs/m/2-24642_imagenes-random-png-cosas-random-png-transparent-png.png",
                "it's a very beautiful day",
                imagesUrl = arrayListOf(
                        "https://androidhdwallpapers.com/media/uploads/2017/02/Yourname-Night-Anime-Sky-Illustration-Art.jpg"
                ),
                shareNum = 10000,
                emojiNum = 10000,
                commentNum= 10000
        )

        val para = "GIÁO VIÊN ĐƯỢC NGHỈ DỊCH Ở NHÀ, HIỆU TRƯỞNG VẪN TRẢ ĐỦ TIỀN LƯƠNG CHO GIÁO VIÊN 100% \n" +
                "Không chỉ là một trong số những trường tư hiếm hoi chi trả 100% tiền lương của cán bộ nhân viên, Hiệu trưởng trường Marie Curie còn gây xúc động mạnh với dòng tin nhắn ân cần gửi đến các giáo viên\n" +
                "Trước diễn biến của dịch bệnh Covid-19, nhiều trường học đã phải thông báo cho học sinh nghỉ tập trung học trong gần 2 tháng qua. Điều này không chỉ ảnh hưởng nặng nề tới quá trình học tập của học sinh mà với các trường tư nhân cũng gánh chịu nhiều hậu quả nặng nề từ nguồn lực tài chính suy giảm.\n" +
                "Học sinh nghỉ học kéo dài, không có nguồn thu, không có tiền trợ cấp… trong khi vẫn phải chi trả tiền mặt bảng khiến nhiều trường tư phải chật vật cân đối chi tiêu. Nhiều trường học đã phải cắt giảm lương, thậm chí không thể trả lương cho giáo viên khiến cho không ít thầy cô phải chật vật kiếm thêm nguồn thu nhập bên ngoài.\n" +
                "Tuy vậy, trường Marie Curie (Hà Nội) vẫn chấp nhận chi trả đầy đủ tất cả lương của giáo viên trong 2 tháng nghỉ dịch. Không những vậy, thầy Nguyễn Xuân Khang (Hiệu trưởng nhà trường) cũng nhắn nhủ mọi người nhớ hạn chế tiêu xài để cầm cự trong lúc khó khăn đồng thời nhớ giữ gìn sức khỏe. Bên cạnh đó, thầy cũng mong các thầy cô tiếp tục giảng dạy và động viên học trò học tập từ xa theo các hình thức học trực tuyến.\n" +
                "Nguyên văn dòng tin nhắn xúc động của Hiệu trưởng trường Marie Curie:\n" +
                "Kính gửi,\n" +
                "Thứ hai 16/3, các cô đến trường phối hợp với Tài vụ làm sổ lương tháng 3. Tôi quyết định, tương tự tháng 2, sẽ phát 100% lương tháng 3 (từ 1/3 đến 31/3/2020) cho bà con trong trường, theo phân công nhiệm vụ từ đầu học kỳ 2. Những ngày đầu tuần tới bà con sẽ nhận được lương tháng 3, sớm hơn bình thường.\n" +
                "Tôi mong muốn hai việc:\n" +
                "1. Các thầy cô giáo tiếp tục giảng dạy và động viên học trò học tập từ xa theo các hình thức online, truyền hình…\n" +
                "2. Mọi người tiêu xài hạn chế để cầm cự trong lúc khó khăn này. Giữ gìn sức khoẻ để phòng, chống dịch Covid-19.\n" +
                "Đã nhiều tuần không gặp nhau, tôi rất nhớ thầy cô và học trò. Tôi vẫn khoẻ, chỉ mong sớm gặp mọi người!\n" +
                "Thân ái!\n" +
                "Hà Nội, ngày 14/3/2020\n" +
                "HT Nguyễn Xuân Khang.\n" +
                "Trong giai đoạn khó khăn, những dòng tin nhắn ân cần từ thầy Hiệu trưởng đã khiến đội ngũ giáo viên vô cùng xúc động khi được quan tâm cả về vật chất lẫn tinh thần. Thầy cũng không quên nhắc nhở chất lượng học tập khi mong muốn thầy cô sẽ tiếp tục lấy đó làm động lực để làm hoàn thành thật tốt việc giảng dạy.\n" +
                "“Một buổi tối thật ấm áp khi nhận được tin nhắn động viên và dặn dò của thầy. Cảm ơn người cha già vĩ đại của con khi đã cho con cơ hội được là thành viên của trường“, cô giáo H.N chia sẻ.\n" +
                "“Liều thuốc tinh thần là đây chứ đâu! Không còn câu từ nào để truyền tải thứ tình yêu này nữa rồi. Em chỉ xin nói trân trọng và biết ơn thôi ạ!“, cô giáo H.P chia sẻ.\n" +
                "theo: Kenh14"

        livePosts.value = arrayListOf(
                post,
                post.copy(id = "2", createdTime = now++, ownerPhone = "0987654321", description = "okay gg !!", ownerName = "it's 9->0"),
                post.copy(id = "3", createdTime = now++, ownerPhone = "0123456789", description = para, ownerName = "1->9 brotherrrrrrrrrrrrrrrrrrrrrrrrrrrr",imagesUrl = null),
                post.copy(id = "4", createdTime = now++, ownerPhone = "0111111111", description = null),
                post.copy(id = "5", createdTime = now, ownerPhone = "0111111111", description = para)
        )

        val story = ImageStory(
                "1",
                now++,
                "0123456789",
                "0->9",
                "https://www.pngfind.com/pngs/m/2-24642_imagenes-random-png-cosas-random-png-transparent-png.png",
                "https://androidhdwallpapers.com/media/uploads/2017/02/Yourname-Night-Anime-Sky-Illustration-Art.jpg"
        )

        liveStories.value = arrayListOf(
                story,
                story.copy(id = "2",createdTime = now++,ownerPhone = "0987654321",ownerName = "9->0"),
                story.copy(id = "3",createdTime = now++,ownerPhone = "0123456789",ownerName = "0->9->"),
                story.copy(id = "4",createdTime = now++,ownerPhone = "0111111111",ownerName = "0111..."),
                story.copy(id = "5",createdTime = now++,ownerPhone = "0987654321",ownerName = "9->0"),
                story.copy(id = "6",createdTime = now,ownerPhone = "0987654321",ownerName = "9->0")
        )
    }
}