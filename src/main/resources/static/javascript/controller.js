const urlParams = new URLSearchParams(window.location.search);

const form = urlParams.get("form") || 1;
const token = urlParams.get("token") || null;

document.querySelector(`#content > div[data-form="${form}"]`).classList.add("show");

document.querySelectorAll("#sidebar a").forEach(anchor => {
    const url = new URL(anchor.href);
    const linkForm = url.searchParams.get("form");
    if (String(linkForm) === String(form)) {
        anchor.classList.add("highlighted");
    }
});

// Signup
const signupForm = document.querySelectorAll("#content > div[data-form=\"1\"] > form");

signupForm[0].addEventListener("submit", event => {
    event.preventDefault();

    emailSend(event.target.email.value);
});

signupForm[1].addEventListener("submit", event => {
    event.preventDefault();

    emailVerify(event.target.email.value, event.target.code.value);
});

signupForm[2].addEventListener("submit", event => {
    event.preventDefault();

    signup(
        event.target.email.value,
        event.target.password.value,
        event.target.nickname.value,
        event.target.name.value
    );
});

// Login
const loginForm = document.querySelectorAll('#content > div[data-form="2"] > form');

loginForm[0].addEventListener("submit", event => {
    event.preventDefault();

    login(
        event.target.email.value,
        event.target.password.value
    );
});

// =====================
// Form 3 - 내 정보 조회
// =====================
const form3 = document.querySelectorAll('#content > div[data-form="3"] > form');

form3[0].addEventListener("submit", event => {
    event.preventDefault();
    info();
});

// =====================
// Form 4 - 내 정보 수정
// =====================
const form4 = document.querySelectorAll('#content > div[data-form="4"] > form');

form4[0].addEventListener("submit", event => {
    event.preventDefault();
    updateInfo(
        event.target.nickname.value,
        event.target.name.value
    );
});

// =====================
// Form 5 - 비밀번호 찾기
// =====================
const form5 = document.querySelectorAll('#content > div[data-form="5"] > form');

form5[0].addEventListener("submit", event => {
    event.preventDefault();
    sendResetPassword(event.target.email.value);
});

if (token) {
    form5[1].token.value = token;
    form5[1].token.disabled = true;
}

form5[1].addEventListener("submit", event => {
    event.preventDefault();
    resetPassword(event.target.token.value, event.target.newPassword.value);
});

// =====================
// Form 6 - 로그아웃
// =====================
const form6 = document.querySelectorAll('#content > div[data-form="6"] > form');

form6[0].addEventListener("submit", event => {
    event.preventDefault();
    logout();
});

// =====================
// Form 7 - 회원탈퇴
// =====================
const form7 = document.querySelectorAll('#content > div[data-form="7"] > form');

form7[0].addEventListener("submit", event => {
    event.preventDefault();
    leave();
});

// =====================
// Form 8 - 게시판 조회
// =====================
const form8 = document.querySelectorAll('#content > div[data-form="8"] > form');

form8[0].addEventListener("submit", event => {
    event.preventDefault();
    getBoardList(
        event.target.page.value || 1,
        event.target.size.value || 10
    );
});

form8[1].addEventListener("submit", event => {
    event.preventDefault();
    getBoard(event.target.id.value);
});

// =====================
// Form 9 - 게시판 생성
// =====================
const form9 = document.querySelectorAll('#content > div[data-form="9"] > form');

form9[0].addEventListener("submit", event => {
    event.preventDefault();
    createBoard(
        event.target.name.value,
        event.target.description.value
    );
});

// =====================
// Form 10 - 게시판 수정
// =====================
const form10 = document.querySelectorAll('#content > div[data-form="10"] > form');

form10[0].addEventListener("submit", event => {
    event.preventDefault();
    updateBoard(
        event.target.id.value,
        event.target.name.value,
        event.target.description.value
    );
});

// =====================
// Form 11 - 게시판 삭제
// =====================
const form11 = document.querySelectorAll('#content > div[data-form="11"] > form');

form11[0].addEventListener("submit", event => {
    event.preventDefault();
    deleteBoard(event.target.id.value);
});

// =====================
// Form 12 - 게시물 조회
// =====================
const form12 = document.querySelectorAll('#content > div[data-form="12"] > form');

form12[0].addEventListener("submit", event => {
    event.preventDefault();
    getPostList(
        event.target.boardId.value,
        event.target.page.value || 1,
        event.target.size.value || 10
    );
});

form12[1].addEventListener("submit", event => {
    event.preventDefault();
    getPost(
        event.target.boardId.value,
        event.target.postId.value
    );
});

// =====================
// Form 13 - 게시물 생성
// =====================
const form13 = document.querySelectorAll('#content > div[data-form="13"] > form');

form13[0].addEventListener("submit", event => {
    event.preventDefault();
    createPost(
        event.target.boardId.value,
        event.target.title.value,
        event.target.content.value
    );
});

// =====================
// Form 14 - 게시물 수정
// =====================
const form14 = document.querySelectorAll('#content > div[data-form="14"] > form');

form14[0].addEventListener("submit", event => {
    event.preventDefault();
    updatePost(
        event.target.boardId.value,
        event.target.postId.value,
        event.target.title.value,
        event.target.content.value
    );
});

// =====================
// Form 15 - 게시물 삭제
// =====================
const form15 = document.querySelectorAll('#content > div[data-form="15"] > form');

form15[0].addEventListener("submit", event => {
    event.preventDefault();
    deletePost(
        event.target.boardId.value,
        event.target.postId.value
    );
});

// =====================
// Form 16 - 댓글 조회
// =====================
const form16 = document.querySelectorAll('#content > div[data-form="16"] > form');

form16[0].addEventListener("submit", event => {
    event.preventDefault();
    getCommentList(
        event.target.boardId.value,
        event.target.postId.value,
        event.target.page.value || 1,
        event.target.size.value || 10
    );
});

form16[1].addEventListener("submit", event => {
    event.preventDefault();
    getComment(
        event.target.boardId.value,
        event.target.postId.value,
        event.target.commentId.value
    );
});

// =====================
// Form 17 - 댓글 작성
// =====================
const form17 = document.querySelectorAll('#content > div[data-form="17"] > form');

form17[0].addEventListener("submit", event => {
    event.preventDefault();
    createComment(
        event.target.boardId.value,
        event.target.postId.value,
        event.target.content.value
    );
});

// =====================
// Form 18 - 댓글 수정
// =====================
const form18 = document.querySelectorAll('#content > div[data-form="18"] > form');

form18[0].addEventListener("submit", event => {
    event.preventDefault();
    updateComment(
        event.target.boardId.value,
        event.target.postId.value,
        event.target.commentId.value,
        event.target.content.value
    );
});

// =====================
// Form 19 - 댓글 삭제
// =====================
const form19 = document.querySelectorAll('#content > div[data-form="19"] > form');

form19[0].addEventListener("submit", event => {
    event.preventDefault();
    deleteComment(
        event.target.boardId.value,
        event.target.postId.value,
        event.target.commentId.value
    );
});

// =====================
// Form 20 - 보낸 쪽지 조회
// =====================
const form20 = document.querySelectorAll('#content > div[data-form="20"] > form');

form20[0].addEventListener("submit", event => {
    event.preventDefault();
    getSentMessages();
});

// =====================
// Form 21 - 받은 쪽지 조회
// =====================
const form21 = document.querySelectorAll('#content > div[data-form="21"] > form');

form21[0].addEventListener("submit", event => {
    event.preventDefault();
    getReceivedMessages();
});

// =====================
// Form 22 - 쪽지 보내기
// =====================
const form22 = document.querySelectorAll('#content > div[data-form="22"] > form');

form22[0].addEventListener("submit", event => {
    event.preventDefault();
    sendMessage(
        event.target.receiverNickname.value,
        event.target.content.value
    );
});

// =====================
// Form 23 - 보낸 쪽지 삭제
// =====================
const form23 = document.querySelectorAll('#content > div[data-form="23"] > form');

form23[0].addEventListener("submit", event => {
    event.preventDefault();
    deleteSentMessage(event.target.id.value);
});

// =====================
// Form 24 - 받은 쪽지 삭제
// =====================
const form24 = document.querySelectorAll('#content > div[data-form="24"] > form');

form24[0].addEventListener("submit", event => {
    event.preventDefault();
    deleteReceivedMessage(event.target.id.value);
});

// =====================
// Form 25 - 문의하기
// =====================
const form25 = document.querySelectorAll('#content > div[data-form="25"] > form');

form25[0].addEventListener("submit", event => {
    event.preventDefault();
    submitSupport(event.target.content.value);
});

// =====================
// Form 26 - 신고하기
// =====================
const form26 = document.querySelectorAll('#content > div[data-form="26"] > form');

form26[0].addEventListener("submit", event => {
    event.preventDefault();
    submitReport(
        event.target.targetId.value,
        event.target.targetType.value,
        event.target.reason.value
    );
});

// =====================
// Form 27 - 대시보드 조회
// =====================
const form27 = document.querySelectorAll('#content > div[data-form="27"] > form');

form27[0].addEventListener("submit", event => {
    event.preventDefault();
    getDashboard();
});

// =====================
// Form 28 - 문의 조회
// =====================
const form28 = document.querySelectorAll('#content > div[data-form="28"] > form');

form28[0].addEventListener("submit", event => {
    event.preventDefault();
    getAdminSupports();
});

form28[1].addEventListener("submit", event => {
    event.preventDefault();
    getAdminSupport(event.target.supportId.value);
});

// =====================
// Form 29 - 신고 조회
// =====================
const form29 = document.querySelectorAll('#content > div[data-form="29"] > form');

form29[0].addEventListener("submit", event => {
    event.preventDefault();
    getAdminReports();
});

form29[1].addEventListener("submit", event => {
    event.preventDefault();
    getAdminReport(event.target.reportId.value);
});

// =====================
// Form 30 - 회원 조회
// =====================
const form30 = document.querySelectorAll('#content > div[data-form="30"] > form');

form30[0].addEventListener("submit", event => {
    event.preventDefault();
    getUserList(
        event.target.page.value || 1,
        event.target.size.value || 10
    );
});

// =====================
// Form 31 - 매니저 관리
// =====================
const form31 = document.querySelectorAll('#content > div[data-form="31"] > form');

form31[0].addEventListener("submit", event => {
    event.preventDefault();
    getBoardManagers(event.target.boardId.value);
});

form31[1].addEventListener("submit", event => {
    event.preventDefault();
    addBoardManager(
        event.target.boardId.value,
        event.target.userId.value
    );
});

form31[2].addEventListener("submit", event => {
    event.preventDefault();
    removeBoardManager(
        event.target.boardId.value,
        event.target.userId.value
    );
});

// =====================
// Form 32 - 계정 잠금
// =====================
const form32 = document.querySelectorAll('#content > div[data-form="32"] > form');

form32[0].addEventListener("submit", event => {
    event.preventDefault();
    updateUserStatus(
        event.target.userId.value,
        event.target.newStatus.value
    );
});

// =====================
// Form 33 - 계정 삭제 (검색)
// =====================
const form33 = document.querySelectorAll('#content > div[data-form="33"] > form');

form33[0].addEventListener("submit", event => {
    event.preventDefault();
    searchUsers(
        event.target.keyword.value,
        event.target.page.value || 1,
        event.target.size.value || 10
    );
});