<template>
  <div id="maintopic">
    <section class="slideshow-full">
      <div class="slideshow-container">
        <div class="slide-track">
          <div v-for="(exhibit, index) in exhibits" :key="index" class="slide">
            <img :src="exhibit.img" :alt="'Slide ' + (index + 1)" />
          </div>
          <div v-for="(exhibit, index) in exhibits" :key="'dup-' + index" class="slide">
            <img :src="exhibit.img" :alt="'Slide ' + (index + 1)" />
          </div>
        </div>
      </div>
    </section>
    <section id ="box-content" >
      <section id="introduction">
        <p class="title" style="font-size: 33px;">Giới thiệu</p>
        <div>
  <p class="content">
    Bảo tàng Quân khu 9 trực thuộc Cục Chính trị Quân khu 9, là một trong những bảo tàng quân sự quan trọng 
    tại khu vực Đồng bằng sông Cửu Long. Bảo tàng được xếp hạng theo hệ thống bảo tàng Việt Nam và đóng vai trò 
    lưu giữ, bảo tồn và phát huy giá trị lịch sử của lực lượng vũ trang miền Tây Nam Bộ qua các thời kỳ.
    Đây là nơi trưng bày hàng nghìn hiện vật, hình ảnh, tư liệu quý giá phản ánh quá trình hình thành, chiến đấu 
    và trưởng thành của quân và dân khu vực Nam Bộ. Các hiện vật không chỉ mang giá trị lịch sử mà còn thể hiện 
    tinh thần yêu nước, ý chí kiên cường và sự hy sinh to lớn của các thế hệ đi trước trong công cuộc bảo vệ Tổ quốc.
    Bảo tàng Quân khu 9 là một quần thể gồm nhiều khu trưng bày chuyên đề khác nhau, mỗi khu vực thể hiện một nội dung 
    riêng biệt nhằm giúp người tham quan có cái nhìn toàn diện về lịch sử và văn hóa của vùng đất Nam Bộ.
  </p>

  <ul style="font-size: 16px;">
    <li>Bảo tàng các lực lượng vũ trang Đồng bằng sông Cửu Long</li>
    <li>Bảo tàng Hồ Chí Minh – chi nhánh Đồng bằng sông Cửu Long (Quân khu 9)</li>
    <li>Phòng trưng bày thân thế và sự nghiệp của Chủ tịch Tôn Đức Thắng</li>
  </ul>

  <p class="content">
    Trong đó, Bảo tàng lực lượng vũ trang Đồng bằng sông Cửu Long được thành lập từ năm 1979, tiền thân là 
    Phòng truyền thống Quân khu 9. Trải qua nhiều năm phát triển, bảo tàng đã không ngừng mở rộng quy mô, nâng cao 
    chất lượng trưng bày nhằm phục vụ tốt hơn nhu cầu tham quan, học tập và nghiên cứu của người dân và du khách.
    Nội dung trưng bày tại bảo tàng được bố trí khoa học theo từng giai đoạn lịch sử, giúp người xem dễ dàng theo dõi 
    và hiểu rõ quá trình phát triển của vùng đất và con người Đồng bằng sông Cửu Long.
  </p>

  <ul style="font-size: 16px;">
    <li>Tóm lược về điều kiện tự nhiên, văn hóa và con người vùng Đồng bằng sông Cửu Long</li>
    <li>Cuộc kháng chiến chống thực dân Pháp xâm lược (1945 – 1954)</li>
    <li>Cuộc kháng chiến chống Mỹ cứu nước (1955 – 1975)</li>
  </ul>

  <p class="content">
    Ngoài các nội dung trưng bày cố định, bảo tàng còn thường xuyên tổ chức các hoạt động triển lãm chuyên đề, 
    giáo dục truyền thống, giao lưu lịch sử và các chương trình trải nghiệm dành cho học sinh, sinh viên và khách du lịch.
    Đây là cầu nối quan trọng giúp thế hệ trẻ hiểu hơn về lịch sử dân tộc và nâng cao ý thức bảo vệ Tổ quốc.
    Với vai trò là một trung tâm văn hóa – lịch sử của khu vực, Bảo tàng Quân khu 9 không chỉ là điểm tham quan 
    hấp dẫn mà còn là nơi lưu giữ ký ức hào hùng của dân tộc, góp phần giáo dục lòng yêu nước và niềm tự hào dân tộc 
    cho các thế hệ hôm nay và mai sau.
  </p>
</div>
      </section>
      <section id="map-section">
        <p class="title" style="font-size: 33px;">Địa điểm tham quan</p>
        <iframe
          width="100%"
          height="90%"
          frameborder="0"
          style="border:0"
          allowfullscreen
          loading="lazy"
          src="https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.91591081713!2d105.782833!3d10.035845!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x31a062a10fa1b08f%3A0xca373128d7e8a849!2zQsOsbyB04buRYyBUaOG7iyBDaMOtbiBLaOG7iyA5!5e0!3m2!1svi!2s!4v1700000000000"
        ></iframe>
      </section>
    </section>
    <section id="list-museaum">
      <div class="museaum-item">
        <a href="https://link1.com" target="_blank">
          <img src="../assets/images/Home/Bao_Tang_HCM.jpg" alt="Bảo tàng 1" />
          <div class="caption">Bảo tàng Hồ Chí Minh</div>
        </a>
      </div>
      <div class="museaum-item">
        <a href="https://link3.com" target="_blank">
          <img src="../assets/images/Home/Bao_Tang_LLVT.jpg" alt="Bảo tàng 3" />
          <div class="caption">Lực lượng vũ trang</div>
        </a>
      </div>
      <div class="museaum-item">
        <a href="https://link2.com" target="_blank">
          <img src="../assets/images/Home/Khu_trung_bay.jpg" alt="Bảo tàng 2" />
          <div class="caption">Khu trưng bày vũ khí</div>
        </a>
      </div>
    </section>
  </div>

</template>

<script>

export default {
  name: "Home",
  setup() {
    const exhibits = []

    for (let i = 1; i <= 8; i++) {
      // Giả sử ảnh đặt tên từ anh_1.jpg → anh_87.jpg trong folder ../assets/images/Home/
      const img = new URL(`../assets/images/Home/anh_${i}.jpg`, import.meta.url).href
      exhibits.push({ img })
    }

    return { exhibits }
  },
};
</script>
<style src="../assets/css/home.css"></style>