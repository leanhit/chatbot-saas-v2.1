#!/bin/bash

# Test address update with proper ownerId and ownerType
curl 'https://chat.truyenthongviet.vn/api/addresses/1' \
  -X 'PUT' \
  -H 'Accept: application/json' \
  -H 'Accept-Language: vi,en;q=0.9' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwaG9uZ3ZhbmhpZXBAZ21haWwuY29tIiwiaWF0IjoxNzcwNTI1NTExLCJleHAiOjE3NzA2MTE5MTF9.6M01mdx3PPbpkBX00o-WzKfkbi_wVthJWehYnX79NbQ' \
  -H 'Connection: keep-alive' \
  -H 'Content-Type: application/json' \
  -H 'Origin: https://cwsv.truyenthongviet.vn' \
  -H 'Referer: https://cwsv.truyenthongviet.vn/' \
  -H 'Sec-Fetch-Dest: empty' \
  -H 'Sec-Fetch-Mode: cors' \
  -H 'Sec-Fetch-Site: same-site' \
  -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36' \
  -H 'X-Tenant-Key: 6b281b36-0ab9-44a3-8b12-a1fe8a24dfae' \
  -H 'sec-ch-ua: "Not(A:Brand";v="8", "Chromium";v="144", "Google Chrome";v="144"' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'sec-ch-ua-platform: "Windows"' \
  --data-raw '{"houseNumber":"yui 3","street":"sssss","ward":"Xã Xuân Lập","district":"Huyện Lâm Bình","province":"Tỉnh Tuyên Quang","country":"Vietnam","fullAddress":"yui 3, sssss, Xã Xuân Lập, Huyện Lâm Bình, Tỉnh Tuyên Quang","ownerId":1,"ownerType":"USER"}'
