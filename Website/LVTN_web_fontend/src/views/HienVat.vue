<template>
  <div id="maintopic">
    <div class="HienVat_BevaDanTenLua">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatTL?.TEN_HIEN_VAT }}</h1>

        <div class="meta-line">
          <span>Năm: {{ hienVatTL?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatTL?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">
        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatTL?.MO_TA }}</p>
        </div>
        <div class="overview-right">
      <section v-if="imagesTL.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesTL"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaTL.length" class="timeline">
        <h2>Chi tiết hiện vật</h2>
        <div class="timeline-wrap">
          <div
            v-for="(item, index) in moTaTL"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>
        </div>
      </section>
      <section v-if="videosTL.length" class="video-section">
        <h2 class="section-title">Video tư liệu</h2>
        <div class="video-grid">
          <iframe
            v-for="video in videosTL"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>
      </section>
    </div>
  </div>
  </div>
  <div class="HienVat_Bom">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatBom?.TEN_HIEN_VAT }}</h1>
        <div class="meta-line">
          <span>Năm: {{ hienVatBom?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatBom?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">
        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatBom?.MO_TA }}</p>
        </div>
        <div class="overview-right">
      <section v-if="imagesBom.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesBom"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaBom.length" class="timeline">
        <h2>Chi tiết hiện vật</h2>
        <div class="timeline-wrap">
          <div
            v-for="(item, index) in moTaBom"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>

        </div>
      </section>
      <section v-if="videosBom.length" class="video-section">
        <h2 class="section-title">Video tư liệu</h2>
        <div class="video-grid">
          <iframe
            v-for="video in videosBom"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>
      </section>
    </div>
  </div>
  </div>
    <div class="HienVat_XeTang">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatXT?.TEN_HIEN_VAT }}</h1>

        <div class="meta-line">
          <span>Năm: {{ hienVatXT?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatXT?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">
        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatXT?.MO_TA }}</p>
        </div>

        <div class="overview-right">
      <section v-if="imagesXT.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesXT"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaXT.length" class="timeline">
        <h2>Chi tiết hiện vật</h2>
        <div class="timeline-wrap">
          <div
            v-for="(item, index) in moTaXT"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>

        </div>
      </section>
      <section v-if="videosXT.length" class="video-section">
        <h2 class="section-title">Video tư liệu</h2>
        <div class="video-grid">
          <iframe
            v-for="video in videosXT"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>
      </section>
    </div>
  </div>
  </div>
    <div class="HienVat_ThuyenGheXuong">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatTGX?.TEN_HIEN_VAT }}</h1>
        <div class="meta-line">
          <span>Năm: {{ hienVatTGX?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatTGX?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">
        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatTGX?.MO_TA }}</p>
        </div>
        <div class="overview-right">
      <section v-if="imagesTGX.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesTGX"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaTGX.length" class="timeline">
        <h2>Chi tiết hiện vật</h2>
        <div class="timeline-wrap">
          <div
            v-for="(item, index) in moTaTGX"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>
            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>
        </div>
      </section>
      <section v-if="videosTGX.length" class="video-section">
        <h2 class="section-title">Video tư liệu</h2>
        <div class="video-grid">
          <iframe
            v-for="video in videosTGX"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>
      </section>
    </div>
  </div>
  </div>
    <div class="HienVat_LuHam">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatLH?.TEN_HIEN_VAT }}</h1>
        <div class="meta-line">
          <span>Năm: {{ hienVatLH?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatLH?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">
        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatLH?.MO_TA }}</p>
        </div>
        <div class="overview-right">
      <section v-if="imagesLH.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesLH"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaLH.length" class="timeline">
        <h2>Chi tiết hiện vật</h2>
        <div class="timeline-wrap">
          <div
            v-for="(item, index) in moTaLH"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>
        </div>
      </section>
      <section v-if="videosLH.length" class="video-section">
        <h2 class="section-title">Video tư liệu</h2>
        <div class="video-grid">
          <iframe
            v-for="video in videosLH"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>
      </section>
    </div>
  </div>
  </div>
    <div class="HienVat_MayCanTol">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatMCT?.TEN_HIEN_VAT }}</h1>
        <div class="meta-line">
          <span>Năm: {{ hienVatMCT?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatMCT?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">

        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatMCT?.MO_TA }}</p>
        </div>
        <div class="overview-right">
      <section v-if="imagesMCT.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesMCT"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaMCT.length" class="timeline">
        <h2>Chi tiết hiện vật</h2>
        <div class="timeline-wrap">
          <div
            v-for="(item, index) in moTaMCT"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>
        </div>
      </section>
      <section v-if="videosMCT.length" class="video-section">
        <h2 class="section-title">Video tư liệu</h2>
        <div class="video-grid">
          <iframe
            v-for="video in videosMCT"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>
      </section>
    </div>
  </div>
  </div>
    <div class="HienVat_MayIn">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatMIP?.TEN_HIEN_VAT }}</h1>
        <div class="meta-line">
          <span>Năm: {{ hienVatMIP?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatMIP?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">
        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatMIP?.MO_TA }}</p>
        </div>
        <div class="overview-right">
      <section v-if="imagesMIP.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesMIP"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaMIP.length" class="timeline">
        <h2>Chi tiết hiện vật</h2>
        <div class="timeline-wrap">
          <div
            v-for="(item, index) in moTaMIP"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>
            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>
        </div>
      </section>
      <section v-if="videosMIP.length" class="video-section">
        <h2 class="section-title">Video tư liệu</h2>
        <div class="video-grid">
          <iframe
            v-for="video in videosMIP"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>
      </section>
    </div>
  </div>
  </div>
    <div class="HienVat_MoNeoTau">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatMNT?.TEN_HIEN_VAT }}</h1>
        <div class="meta-line">
          <span>Năm: {{ hienVatMNT?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatMNT?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">
        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatMNT?.MO_TA }}</p>
        </div>
        <div class="overview-right">
      <section v-if="imagesMNT.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesMNT"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaMNT.length" class="timeline">
        <h2>Chi tiết hiện vật</h2>
        <div class="timeline-wrap">
          <div
            v-for="(item, index) in moTaMNT"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>
            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>
        </div>
      </section>
      <section v-if="videosMNT.length" class="video-section">
        <h2 class="section-title">Video tư liệu</h2>
        <div class="video-grid">
          <iframe
            v-for="video in videosMNT"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>
      </section>
    </div>
  </div>
  </div>
    <div class="HienVat_Phao">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatPH?.TEN_HIEN_VAT }}</h1>
        <div class="meta-line">
          <span>Năm: {{ hienVatPH?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatPH?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">
        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatPH?.MO_TA }}</p>
        </div>
        <div class="overview-right">
      <section v-if="imagesPH.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesPH"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaPH.length" class="timeline">
        <h2>Chi tiết hiện vật</h2>
        <div class="timeline-wrap">
          <div
            v-for="(item, index) in moTaPH"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>
        </div>
      </section>
      <section v-if="videosPH.length" class="video-section">
        <h2 class="section-title">Video tư liệu</h2>
        <div class="video-grid">
          <iframe
            v-for="video in videosPH"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>
      </section>
    </div>
  </div>
  </div>
    <div class="HienVat_SungThanCong">
    <div class="artifact-page">
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatSTC?.TEN_HIEN_VAT }}</h1>
        <div class="meta-line">
          <span>Năm: {{ hienVatSTC?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatSTC?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>
    <div v-if="loading" class="loading">
      Đang tải dữ liệu
    </div>
    <div v-else>
      <section class="overview">
        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatSTC?.MO_TA }}</p>
        </div>
        <div class="overview-right">
      <section v-if="imagesSTC.length">
        <h2>Hình ảnh tư liệu</h2>
        <div class="gallery-track">
          <div
            v-for="img in imagesSTC"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>
      </section>
        </div>
      </section>
      <section v-if="moTaSTC.length" class="timeline">

        <h2>Chi tiết hiện vật</h2>

        <div class="timeline-wrap">

          <div
            v-for="(item, index) in moTaSTC"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>

        </div>
      </section>

      <!-- VIDEO FLOAT -->
      <section v-if="videosSTC.length" class="video-section">

        <h2 class="section-title">Video tư liệu</h2>

        <div class="video-grid">
          <iframe
            v-for="video in videosSTC"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>

      </section>

    </div>
  </div>
  </div>
    <div class="HienVat_TauTuanTieu">
    <div class="artifact-page">

    <!-- HERO -->
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatTTT?.TEN_HIEN_VAT }}</h1>

        <div class="meta-line">
          <span>Năm: {{ hienVatTTT?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatTTT?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading">
      Đang tải dữ liệu...
    </div>

    <div v-else>

      <!-- OVERVIEW SPLIT -->
      <section class="overview">

        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatTTT?.MO_TA }}</p>
        </div>

        <div class="overview-right">
      <!-- GALLERY SCROLL -->
      <section v-if="imagesTTT.length">

        <h2>Hình ảnh tư liệu</h2>

        <div class="gallery-track">
          <div
            v-for="img in imagesTTT"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>

      </section>
        </div>

      </section>

      <!-- DETAIL FLOW -->
      <section v-if="moTaTTT.length" class="timeline">

        <h2>Chi tiết hiện vật</h2>

        <div class="timeline-wrap">

          <div
            v-for="(item, index) in moTaTTT"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>

        </div>
      </section>

      <!-- VIDEO FLOAT -->
      <section v-if="videosTTT.length" class="video-section">

        <h2 class="section-title">Video tư liệu</h2>

        <div class="video-grid">
          <iframe
            v-for="video in videosTTT"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>

      </section>

    </div>
  </div>
  </div>
    <div class="HienVat_TrucMayBay">
    <div class="artifact-page">

    <!-- HERO -->
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatTMB?.TEN_HIEN_VAT }}</h1>

        <div class="meta-line">
          <span>Năm: {{ hienVatTMB?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatTMB?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading">
      Đang tải dữ liệu...
    </div>

    <div v-else>

      <!-- OVERVIEW SPLIT -->
      <section class="overview">

        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatTMB?.MO_TA }}</p>
        </div>

        <div class="overview-right">
      <!-- GALLERY SCROLL -->
      <section v-if="imagesTMB.length">

        <h2>Hình ảnh tư liệu</h2>

        <div class="gallery-track">
          <div
            v-for="img in imagesTMB"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>

      </section>
        </div>

      </section>

      <!-- DETAIL FLOW -->
      <section v-if="moTaTMB.length" class="timeline">

        <h2>Chi tiết hiện vật</h2>

        <div class="timeline-wrap">

          <div
            v-for="(item, index) in moTaTMB"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>

        </div>
      </section>

      <!-- VIDEO FLOAT -->
      <section v-if="videosTMB.length" class="video-section">

        <h2 class="section-title">Video tư liệu</h2>

        <div class="video-grid">
          <iframe
            v-for="video in videosTMB"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>

      </section>

    </div>
  </div>
  </div>
    <div class="HienVat_Xebocthep">
    <div class="artifact-page">

    <!-- HERO -->
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatXBT?.TEN_HIEN_VAT }}</h1>

        <div class="meta-line">
          <span>Năm: {{ hienVatXBT?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatXBT?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading">
      Đang tải dữ liệu...
    </div>

    <div v-else>

      <!-- OVERVIEW SPLIT -->
      <section class="overview">

        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatXBT?.MO_TA }}</p>
        </div>

        <div class="overview-right">
      <!-- GALLERY SCROLL -->
      <section v-if="imagesXBT.length">

        <h2>Hình ảnh tư liệu</h2>

        <div class="gallery-track">
          <div
            v-for="img in imagesXBT"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>

      </section>
        </div>

      </section>

      <!-- DETAIL FLOW -->
      <section v-if="moTaXBT.length" class="timeline">

        <h2>Chi tiết hiện vật</h2>

        <div class="timeline-wrap">

          <div
            v-for="(item, index) in moTaXBT"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>

        </div>
      </section>

      <!-- VIDEO FLOAT -->
      <section v-if="videosXBT.length" class="video-section">

        <h2 class="section-title">Video tư liệu</h2>

        <div class="video-grid">
          <iframe
            v-for="video in videosXBT"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>

      </section>

    </div>
  </div>
  </div>

    <div class="HienVat_peugeot">
    <div class="artifact-page">

    <!-- HERO -->
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatXP?.TEN_HIEN_VAT }}</h1>

        <div class="meta-line">
          <span>Năm: {{ hienVatXP?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatXP?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading">
      Đang tải dữ liệu...
    </div>

    <div v-else>

      <!-- OVERVIEW SPLIT -->
      <section class="overview">

        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatXP?.MO_TA }}</p>
        </div>

        <div class="overview-right">
      <!-- GALLERY SCROLL -->
      <section v-if="imagesXP.length">

        <h2>Hình ảnh tư liệu</h2>

        <div class="gallery-track">
          <div
            v-for="img in imagesXP"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>

      </section>
        </div>

      </section>

      <!-- DETAIL FLOW -->
      <section v-if="moTaXP.length" class="timeline">

        <h2>Chi tiết hiện vật</h2>

        <div class="timeline-wrap">

          <div
            v-for="(item, index) in moTaXP"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>

        </div>
      </section>

      <!-- VIDEO FLOAT -->
      <section v-if="videosXP.length" class="video-section">

        <h2 class="section-title">Video tư liệu</h2>

        <div class="video-grid">
          <iframe
            v-for="video in videosXP"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>

      </section>

    </div>
  </div>
  </div>
    <div class="HienVat_maybay">
    <div class="artifact-page">

    <!-- HERO -->
    <header class="hero">
      <div class="hero-inner">
        <div class="tag">HIỆN VẬT TRƯNG BÀY</div>
        <h1 class="title">{{ hienVatMB?.TEN_HIEN_VAT }}</h1>

        <div class="meta-line">
          <span>Năm: {{ hienVatMB?.NAM }}</span>
          <span>•</span>
          <span>{{ hienVatMB?.NGUON_GOC }}</span>
        </div>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="loading" class="loading">
      Đang tải dữ liệu...
    </div>

    <div v-else>

      <!-- OVERVIEW SPLIT -->
      <section class="overview">

        <div class="overview-left">
          <h2>Giới thiệu</h2>
          <p>{{ hienVatMB?.MO_TA }}</p>
        </div>

        <div class="overview-right">
      <!-- GALLERY SCROLL -->
      <section v-if="imagesMB.length">

        <h2>Hình ảnh tư liệu</h2>

        <div class="gallery-track">
          <div
            v-for="img in imagesMB"
            :key="img.MA_ANH"
            class="gallery-item"
          >
            <img :src="img.URL_ANH" />
          </div>
        </div>

      </section>
        </div>

      </section>

      <!-- DETAIL FLOW -->
      <section v-if="moTaMB.length" class="timeline">

        <h2>Chi tiết hiện vật</h2>

        <div class="timeline-wrap">

          <div
            v-for="(item, index) in moTaMB"
            :key="item.MA_MO_TA"
            class="timeline-item"
          >
            <div class="dot">{{ index + 1 }}</div>

            <div class="content">
              <h3>{{ item.TIEU_DE }}</h3>
              <p>{{ item.NOI_DUNG }}</p>
            </div>
          </div>

        </div>
      </section>

      <!-- VIDEO FLOAT -->
      <section v-if="videosMB.length" class="video-section">

        <h2 class="section-title">Video tư liệu</h2>

        <div class="video-grid">
          <iframe
            v-for="video in videosMB"
            :key="video.MA_VIDEO"
            class="video"
            :src="convertYoutube(video.URL_VIDEO)"
            frameborder="0"
            allowfullscreen
          ></iframe>
        </div>

      </section>

    </div>
  </div>
  </div>



</div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      // BOM
      hienVatBom: null,
      moTaBom: [],
      imagesBom: [],
      videosBom: [],

      // XE TĂNG
      hienVatXT: null,
      moTaXT: [],
      imagesXT: [],
      videosXT: [],

      loading: true,

      // BE & DAN TEN LUA
      hienVatTL: null,
      moTaTL: [],
      imagesTL: [],
      videosTL: [],

      loading: true,
      // THUYEN - GHE - XUONG
      hienVatTGX: null,
      moTaTGX: [],
      imagesTGX: [],
      videosTGX: [],

      loading: true,
            // LU-Ham
      hienVatLH: null,
      moTaLH: [],
      imagesLH: [],
      videosLH: [],

      loading: true,
      // MAY CAN TOL
      hienVatMCT: null,
      moTaMCT: [],
      imagesMCT: [],
      videosMCT: [],

      loading: true,
      // MAY IN
      hienVatMIP: null,
      moTaMIP: [],
      imagesMIP: [],
      videosMIP: [],

      loading: true,
      // MO NEO TAU
      hienVatMNT: null,
      moTaMNT: [],
      imagesMNT: [],
      videosMNT: [],

      loading: true,
      // MO NEO TAU
      hienVatPH: null,
      moTaPH: [],
      imagesPH: [],
      videosPH: [],

      loading: true,

      // MO NEO TAU
      hienVatSTC: null,
      moTaSTC: [],
      imagesSTC: [],
      videosSTC: [],

      loading: true,

      // TAU TUAN TIEU
      hienVatTTT: null,
      moTaTTT: [],
      imagesTTT: [],
      videosTTT: [],

      loading: true,
      // TRUC MAY BAY
      hienVatTMB: null,
      moTaTMB: [],
      imagesTMB: [],
      videosTMB: [],

      loading: true,
            // XE BOC THEP
      hienVatXBT: null,
      moTaXBT: [],
      imagesXBT: [],
      videosXBT: [],

      loading: true,
      // XE peugeot
      hienVatXP: null,
      moTaXXP: [],
      imagesXP: [],
      videosXP: [],

      loading: true,

      // MAY BAY TRUC THANG
      hienVatMB: null,
      moTaXMB: [],
      imagesMB: [],
      videosMB: [],

      loading: true,
    };
  },

  async mounted() {
    await Promise.all([
      this.fetchDataBOM(),
      this.fetchDataXETANG(),
      this.fetchDataTL(),
      this.fetchDataTGX(),
      this.fetchDataLH(),
      this.fetchDataMCT(),
      this.fetchDataMIP(),
      this.fetchDataMNT(),
      this.fetchDataPH(),
      this.fetchDataSTC(),
      this.fetchDataTTT(),
      this.fetchDataTMB(),
      this.fetchDataXBT(),
      this.fetchDataXP(),
      this.fetchDataMB()
    ]);
    this.loading = false;
  },

  methods: {
    async fetchDataBOM() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/1");

        this.hienVatBom = res.data.hien_vat;
        this.moTaBom = res.data.mo_ta;
        this.imagesBom = res.data.hinh_anh;
        this.videosBom = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataXETANG() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/2");

        this.hienVatXT = res.data.hien_vat;
        this.moTaXT = res.data.mo_ta;
        this.imagesXT = res.data.hinh_anh;
        this.videosXT = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataTL() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/3");

        this.hienVatTL = res.data.hien_vat;
        this.moTaTL = res.data.mo_ta;
        this.imagesTL = res.data.hinh_anh;
        this.videosTL = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataTGX() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/5");

        this.hienVatTGX = res.data.hien_vat;
        this.moTaTGX = res.data.mo_ta;
        this.imagesTGX = res.data.hinh_anh;
        this.videosTGX = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataLH() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/6");

        this.hienVatLH = res.data.hien_vat;
        this.moTaLH = res.data.mo_ta;
        this.imagesLH = res.data.hinh_anh;
        this.videosLH = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataMCT() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/7");

        this.hienVatMCT = res.data.hien_vat;
        this.moTaMCT = res.data.mo_ta;
        this.imagesMCT = res.data.hinh_anh;
        this.videosMCT = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataMIP() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/8");

        this.hienVatMIP = res.data.hien_vat;
        this.moTaMIP = res.data.mo_ta;
        this.imagesMIP = res.data.hinh_anh;
        this.videosMIP = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataMNT() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/9");

        this.hienVatMNT = res.data.hien_vat;
        this.moTaMNT = res.data.mo_ta;
        this.imagesMNT = res.data.hinh_anh;
        this.videosMNT = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataPH() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/10");

        this.hienVatPH = res.data.hien_vat;
        this.moTaPH = res.data.mo_ta;
        this.imagesPH = res.data.hinh_anh;
        this.videosPH = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataSTC() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/11");

        this.hienVatSTC = res.data.hien_vat;
        this.moTaSTC = res.data.mo_ta;
        this.imagesSTC = res.data.hinh_anh;
        this.videosSTC = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataTTT() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/12");

        this.hienVatTTT = res.data.hien_vat;
        this.moTaTTT = res.data.mo_ta;
        this.imagesTTT = res.data.hinh_anh;
        this.videosTTT = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataTMB() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/13");

        this.hienVatTMB = res.data.hien_vat;
        this.moTaTMB = res.data.mo_ta;
        this.imagesTMB = res.data.hinh_anh;
        this.videosTMB = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataXBT() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/14");

        this.hienVatXBT = res.data.hien_vat;
        this.moTaXBT = res.data.mo_ta;
        this.imagesXBT = res.data.hinh_anh;
        this.videosXBT = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataXP() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/15");

        this.hienVatXP = res.data.hien_vat;
        this.moTaXP = res.data.mo_ta;
        this.imagesXP = res.data.hinh_anh;
        this.videosXP = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    async fetchDataMB() {
      try {
        const res = await axios.get("http://localhost:3000/api/hienvat/16");

        this.hienVatMB = res.data.hien_vat;
        this.moTaMB = res.data.mo_ta;
        this.imagesMB = res.data.hinh_anh;
        this.videosMB = res.data.video;

      } finally {
        this.loading = false;
      }
    },
    convertYoutube(url) {
      if (!url) return "";
      return url.replace("watch?v=", "embed/");
    }
  },
  
};
</script>

<style src="../assets/css/hienvat.css"></style>