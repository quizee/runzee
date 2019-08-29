<refactoring 주된 내용>
- 변수명에 팟홀표기법과 카멜표기법을 혼용하여 일관성이 떨어지므로 카멜표기법으로 통일
- 코드가 중복되는 것을 발견하고 함수를 만들어 재사용 
- 객체 안의 메소드나 멤벼 변수로 들어가는 행위를 반복하여 가독성이 떨어지는 경우, 중간 변수로 재정의
- 역할이 비슷한 코드들을 그대로 나열하기보다 함수로 떼어 기능단위로 묶기
- 한 함수가 한 페이지를 넘어가는 경우 특정 부분을 함수로 떼어 짧게 만들기 

1. TodayActivity 
- 연속된 좌표들이 담긴 리스트에서 특정 index에 대한 좌표(위도,경도)를 꺼내려는 코드가 중복되는 것을 발견하고 함수를 만들어 재사용 
- 중간 변수 중 대표적으로는 ReadyActivity.runningItem.getTrakerPoints() 를 trakerPoints 라는 변수로 재정의
- 런닝에 대한 평가를 하는 코드들(역할이 비슷한 코드들)을 함수로 떼어내어 호출
  
2. AddFriendActivity 
- 리사이클러뷰에 새로운 어댑터를 붙여야하는 경우가 중복되어 이를 함수로 만들어 재사용 
- 기존에 추가되었던 친구인지 아닌지에 따라 다른 처리를 해주는 부분이 너무 길어져 한 화면에 들어오도록 만들기 위해 함수 정의
(기존에 추가되었던 친구인 경우와 추가된 적이 없는 친구인 경우를 다루는 함수를 각자 만들어 호출)
- 데이터베이스에 접근시 각 reference를 중간변수로 나누어서 접근
- 조건문을 쓸 때 경우를 비효율적으로 나누어 깊이가 더 들어간 경우가 있었음. 분기되는 조건을 달리하여 깊이를 덜어냄

3. ReadyActivity 
- 분 단위의 시간이 주어졌을 때 적절한 string을 리턴하는 함수를 만들어 재사용 

4. ClubActivity 
- 친구의 친구 목록을 불러와 추천 친구 해시맵을 만들어주는 함수를 따로 만들어 깊이를 줄임
- 서로 아는 친구 명수 순서로 정렬하는 함수를 정의하여 깊이를 줄임
- 검색어에 따라 결과를 리스트에 담는 함수를 정의하여 가독성을 높임



