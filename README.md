# filter_cv_service 
## version 1
```sh
Service được cài đặt trên localhost:8080
(http://localhost:8080//PDF_Filter_RESTful/FilterProcess)
```
## Hướng dẫn sử dụng
> Request body (form request, multipart):
>> Danh sách CV cần lọc: **Key:** attachment1, attachment2, ..., attachmentn. **Value:**: file <br>
>> Số lượng CV cần lọc: **Key:** file_count. **Value:** n <br>
>> Danh sách tiêu chí (json): **Key:** criteria. **Value:** {"data": []} <br>
>> Cho phép thông báo: **Key:** enable_notification. **Value:** true/false <br><br>

> Response:
>> Danh sách CV phù hợp và không phù hợp
