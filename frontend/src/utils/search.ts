import moment from 'moment';

function removeAccents(str: string) {
    return str.normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/đ/g, 'd').replace(/Đ/g, 'D');
}

function filterDataFunction(inputStr: string, inputArr: any[]): any[] {
    //console.log("filterDataFunction", inputStr, inputArr);
    const temp = [];
    const searchTxt = removeAccents(inputStr);
    inputArr.forEach((item: any) => {
        const searchIn = removeAccents(
            item?.botId + " " +
            item?.name + " " +
            item?.phone + " " +
            item?.phoneNumber + " " +
            item?.botName + " " 
        );
        if (searchIn.toLowerCase().includes(searchTxt.toLocaleLowerCase())) {
            temp.push(item);
        }
    });
    return temp;
}

function filterFunction(inputStr: string, inputArr: any[]): any[] {
    const temp = [];
    inputArr.forEach((item: any) => {
        const searchIn = 
            item?.botId + " " +
            item?.name + " " +
            item?.phone + " " +
            item?.phoneNumber + " " +
            item?.botName + " " 
        
        if (searchIn.includes(inputStr)) {
            temp.push(item);
        }
    });
    return temp;
}

/**
 * PHIÊN BẢN ĐÃ SỬA: Phân trang bằng hàm slice() đơn giản và an toàn hơn.
 * @param inputArr Mảng dữ liệu cần phân trang (dữ liệu gốc hoặc đã lọc).
 * @param pagination Object chứa pageSize và currentPage.
 * @returns Mảng dữ liệu đã được cắt theo trang.
 */
function splitData(inputArr: any[], pagination: any): any[] {
    // totalItems đã được cập nhật trước khi gọi hàm này, nên không cần tính toán lại maxPage
    
    // Tính điểm bắt đầu: ví dụ (trang 1 - 1) * 15 = 0; (trang 2 - 1) * 15 = 15
    const startPoint = pagination.pageSize * (pagination.currentPage - 1);
    
    // Tính điểm kết thúc: startPoint + pageSize
    const endPoint = startPoint + pagination.pageSize;
    
    // Dùng slice để cắt mảng. Hàm slice tự động xử lý khi endPoint vượt quá độ dài mảng.
    const temp = inputArr.slice(startPoint, endPoint);

    // Kiểm tra và in log nếu trang hiện tại vượt quá giới hạn
    if (startPoint >= inputArr.length && inputArr.length > 0) {
         console.warn(`Attempted to access page ${pagination.currentPage} with pageSize ${pagination.pageSize}, but data ended at index ${inputArr.length - 1}. Returning empty array.`);
         // Trong list.ts, chúng ta đã reset currentPage về 1 khi cần, 
         // nhưng điều này giúp debug nếu có lỗi logic khác.
    }
    
    return temp;
}

function formatDateTime(dateTime: Date | string){
    if (!dateTime) return ''; // Xử lý trường hợp dateTime rỗng
    return moment(dateTime).calendar(null, {
        sameDay: '[] HH:mm:ss',
        lastDay: '[Hôm qua] HH:mm:ss',
        nextDay: '[Ngày mai] HH:mm:ss',
        lastWeek: 'DD/MM/YYYY HH:mm:ss',
        sameElse: 'DD/MM/YYYY HH:mm:ss',
    });
}

export {
    filterFunction,
    filterDataFunction,
    splitData,
    formatDateTime
}