document.addEventListener("DOMContentLoaded", () => {
    const plainTextArea = document.querySelector('#plaintext');        
    const imagesInput = document.querySelector('#images_folder');      
    const cipherTextArea = document.querySelector('#ciphertext');      
    const imgRow = document.querySelector('.img-row');                 
    const encryptBtn = document.querySelector('#encrypt_btn');         
    const decryptBtn = document.querySelector('#decrypt_btn'); 
    const downloadKeyBtn = document.querySelector('#download_key_btn');   
    const plainTextDecArea = document.querySelector('#phaintextdec'); 
    let latestKeyFile = "";   
    let lastPlainText = "";  
    encryptBtn.addEventListener("click", async () => {
        const plainText = plainTextArea.value;
        const files = Array.from(imagesInput.files);

        if (!plainText && files.length === 0) {
            alert("Vui lòng nhập plaintext hoặc chọn folder ảnh!");
            return;
        }

        const formData = new FormData();
        formData.append("plain_text", plainText);
        files.forEach(file => formData.append("images", file));

        try {
            const res = await fetch("http://localhost:8000/encrypt", {
                method: "POST",
                body: formData
            });

            const data = await res.json();

            if(data.status !== "success"){
                alert("Lỗi: " + data.detail);
                return;
            }
            lastPlainText = data.plain_test;
            cipherTextArea.value = data.cipher_text;

            imgRow.innerHTML = "";

            const beforeImgs = data.before_images;
            const afterImgs = data.after_images;

            const divOrig = document.createElement("div");
            divOrig.className = "img-box";
            const imgOrig = document.createElement("img");
            imgOrig.src = beforeImgs[0];
            imgOrig.style.width = "100%";
            imgOrig.style.height = "100%";
            divOrig.appendChild(imgOrig);

            const divWM = document.createElement("div");
            divWM.className = "img-box";
            const imgWM = document.createElement("img");
            imgWM.src = afterImgs[0];
            imgWM.style.width = "100%";
            imgWM.style.height = "100%";
            divWM.appendChild(imgWM);

            imgRow.appendChild(divOrig);
            imgRow.appendChild(divWM);

            latestKeyFile = data.key_file.split("/").pop();

            alert("Mã hóa thành công! Ảnh demo đã hiển thị.");

        } catch (err) {
            console.error(err);
            alert("Có lỗi xảy ra khi gửi dữ liệu lên API!");
        }
        
    });

    downloadKeyBtn.addEventListener("click", () => {
        if (!latestKeyFile) {
            alert("Chưa có file key để tải. Vui lòng mã hóa trước!");
            return;
        }
        window.open("http://localhost:8000/download/key.txt");
    });

    decryptBtn.addEventListener("click", async () => {
        if(lastPlainText != null){
            plainTextDecArea.value = lastPlainText;
            alert("Đã giải mã thành công");
        }
        else{
            alert("Giải mã thất bại!!")
        }
        
    });

});
