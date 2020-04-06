package vng.zalo.tdtai.zalo.ui.home.diary

import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.util.Constants
import javax.inject.Inject

class DiaryViewModel @Inject constructor() : BaseViewModel() {
    val liveDiaries = MutableLiveData(ArrayList<Diary>())
    val liveStoryGroups: MutableLiveData<List<StoryGroup>> = MutableLiveData(ArrayList())

    val liveIsAnyStory: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        var now = System.currentTimeMillis()

        val post = Diary(
                "1",
                now++,
                "0123456789",
                "1 to 9 bro",
                "https://www.pngfind.com/pngs/m/2-24642_imagenes-random-png-cosas-random-png-transparent-png.png",
                "it's a very beautiful day",
                imagesUrl = arrayListOf(
                        "https://androidhdwallpapers.com/media/uploads/2017/02/Yourname-Night-Anime-Sky-Illustration-Art.jpg"
                ),
                shareNumCount = 10000,
                reactCount = 10000,
                commentCount = 10000
        )

        val para = "GIÁO VIÊN ĐƯỢC NGHỈ DỊCH Ở NHÀ, HIỆU TRƯỞNG VẪN TRẢ ĐỦ TIỀN LƯƠNG CHO GIÁO VIÊN 100% \n\n" +
                "Không chỉ là một trong số những trường tư hiếm hoi chi trả 100% tiền lương của cán bộ nhân viên, Hiệu trưởng trường Marie Curie còn gây xúc động mạnh với dòng tin nhắn ân cần gửi đến các giáo viên\n\n" +
                "Trước diễn biến của dịch bệnh Covid-19, nhiều trường học đã phải thông báo cho học sinh nghỉ tập trung học trong gần 2 tháng qua. Điều này không chỉ ảnh hưởng nặng nề tới quá trình học tập của học sinh mà với các trường tư nhân cũng gánh chịu nhiều hậu quả nặng nề từ nguồn lực tài chính suy giảm.\n\n" +
                "Học sinh nghỉ học kéo dài, không có nguồn thu, không có tiền trợ cấp… trong khi vẫn phải chi trả tiền mặt bảng khiến nhiều trường tư phải chật vật cân đối chi tiêu. Nhiều trường học đã phải cắt giảm lương, thậm chí không thể trả lương cho giáo viên khiến cho không ít thầy cô phải chật vật kiếm thêm nguồn thu nhập bên ngoài.\n\n" +
                "Tuy vậy, trường Marie Curie (Hà Nội) vẫn chấp nhận chi trả đầy đủ tất cả lương của giáo viên trong 2 tháng nghỉ dịch. Không những vậy, thầy Nguyễn Xuân Khang (Hiệu trưởng nhà trường) cũng nhắn nhủ mọi người nhớ hạn chế tiêu xài để cầm cự trong lúc khó khăn đồng thời nhớ giữ gìn sức khỏe. Bên cạnh đó, thầy cũng mong các thầy cô tiếp tục giảng dạy và động viên học trò học tập từ xa theo các hình thức học trực tuyến.\n\n" +
                "Nguyên văn dòng tin nhắn xúc động của Hiệu trưởng trường Marie Curie:\n\n" +
                "Kính gửi,\n\n" +
                "Thứ hai 16/3, các cô đến trường phối hợp với Tài vụ làm sổ lương tháng 3. Tôi quyết định, tương tự tháng 2, sẽ phát 100% lương tháng 3 (từ 1/3 đến 31/3/2020) cho bà con trong trường, theo phân công nhiệm vụ từ đầu học kỳ 2. Những ngày đầu tuần tới bà con sẽ nhận được lương tháng 3, sớm hơn bình thường.\n\n" +
                "Tôi mong muốn hai việc:\n\n" +
                "1. Các thầy cô giáo tiếp tục giảng dạy và động viên học trò học tập từ xa theo các hình thức online, truyền hình…\n\n" +
                "2. Mọi người tiêu xài hạn chế để cầm cự trong lúc khó khăn này. Giữ gìn sức khoẻ để phòng, chống dịch Covid-19.\n\n" +
                "Đã nhiều tuần không gặp nhau, tôi rất nhớ thầy cô và học trò. Tôi vẫn khoẻ, chỉ mong sớm gặp mọi người!\n\n" +
                "Thân ái!\n\n" +
                "Hà Nội, ngày 14/3/2020\n\n" +
                "HT Nguyễn Xuân Khang.\n\n" +
                "Trong giai đoạn khó khăn, những dòng tin nhắn ân cần từ thầy Hiệu trưởng đã khiến đội ngũ giáo viên vô cùng xúc động khi được quan tâm cả về vật chất lẫn tinh thần. Thầy cũng không quên nhắc nhở chất lượng học tập khi mong muốn thầy cô sẽ tiếp tục lấy đó làm động lực để làm hoàn thành thật tốt việc giảng dạy.\n\n" +
                "“Một buổi tối thật ấm áp khi nhận được tin nhắn động viên và dặn dò của thầy. Cảm ơn người cha già vĩ đại của con khi đã cho con cơ hội được là thành viên của trường“, cô giáo H.N chia sẻ.\n\n" +
                "“Liều thuốc tinh thần là đây chứ đâu! Không còn câu từ nào để truyền tải thứ tình yêu này nữa rồi. Em chỉ xin nói trân trọng và biết ơn thôi ạ!“, cô giáo H.P chia sẻ.\n\n" +
                "theo: Kenh14"

        liveDiaries.value = arrayListOf(
                post,
                post.copy(id = "2", createdTime = now++, ownerId = "0987654321", text = "okay gg !!", ownerName = "it's 9->0"),
                post.copy(id = "3", createdTime = now++, ownerId = "0123456789", text = para, ownerName = "1->9 brotherrrrrrrrrrrrrrrrrrrrrrrrrrrr", imagesUrl = arrayListOf()),
                post.copy(id = "4", createdTime = now++, ownerId = "0111111111", text = ""),
                post.copy(id = "5", createdTime = now, ownerId = "0111111111", text = para)
        )

        refreshRecentStoryGroup()
        listenForNewStory()
    }

    fun refreshRecentStoryGroup() {
        database.getUsersRecentStoryGroup(
                arrayListOf("0123456789", "0987654321", "0111111111")
        ) { storyGroups ->
            liveStoryGroups.value = storyGroups.sortedWith(Comparator { o1, o2 ->
                val curUserId = sessionManager.curUser!!.id
                return@Comparator when {
                    o1.ownerId == curUserId -> {
                        -1
                    }
                    o2.ownerId == curUserId -> {
                        1
                    }
                    else -> o2.createdTime!!.compareTo(o1.createdTime!!)
                }
            })
        }
    }

    private fun listenForNewStory() {
        listenerRegistrations.add(
                database.addUserLastStoryCreatedTimeListener(sessionManager.curUser!!.id!!) {
                    liveIsAnyStory.value = it != null && it > System.currentTimeMillis() - Constants.DEFAULT_STORY_LIVE_TIME
                }
        )
    }
}