/* =====================================================================
   commands.js  —  API 함수 모음
   ===================================================================== */

let BASE_URL = '';

function setBaseUrl(url) {
    BASE_URL = (url || '').replace(/\/$/, '');
}

/* ── 공통 fetch 래퍼 ── */
async function api(method, url, body, isForm) {
    const fullUrl = BASE_URL + url;
    const options = { method, credentials: 'include', headers: {} };

    if (body !== undefined) {
        if (isForm) {
            options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
            options.body = new URLSearchParams(body);
        } else {
            options.headers['Content-Type'] = 'application/json';
            options.body = typeof body === 'string' ? body : JSON.stringify(body);
        }
    }

    const response = await fetch(fullUrl, options);
    let data = null;
    try { data = await response.json(); } catch (_) {}
    return { status: response.status, ok: response.ok, data };
}

/* ── 사용자 ── */
async function emailSend(email)                   { return api('POST', `/api/mail/send?email=${encodeURIComponent(email)}`); }
async function emailVerify(email, code)           { return api('POST', `/api/mail/verify?email=${encodeURIComponent(email)}&code=${encodeURIComponent(code)}`); }
async function signup(email, pw, nickname, name)  { return api('POST', '/account/signup', { email, password: pw, nickname, name }); }
async function login(email, pw)                   { return api('POST', '/account/login', { username: email, password: pw }, true); }
async function logout()                           { return api('POST', '/account/logout'); }
async function info()                             { return api('GET',  '/account/info'); }
async function updateInfo(nickname, name)         { return api('PUT',  '/account/info/update', { nickname, name }); }
async function sendResetPassword(email)           { return api('POST', '/account/password/send-link', { email }); }
async function resetPassword(token, newPassword)  { return api('POST', '/account/password/reset', { token, newPassword }); }
async function leave()                            { return api('DELETE', '/account'); }

/* ── 게시판 ── */
async function getBoardList(page = 1, size = 10)          { return api('GET',    `/boards?page=${page}&size=${size}`); }
async function getBoard(id)                               { return api('GET',    `/boards/${id}`); }
async function createBoard(name, description)             { return api('POST',   '/boards', { name, description }); }
async function updateBoard(id, name, description)         { return api('PUT',    `/boards/${id}`, { name, description }); }
async function deleteBoard(id)                            { return api('DELETE', `/boards/${id}`); }

/* ── 게시물 ── */
async function getPostList(boardId, page = 1, size = 10)  { return api('GET',    `/boards/${boardId}/posts?page=${page}&size=${size}`); }
async function getPost(boardId, postId)                   { return api('GET',    `/boards/${boardId}/posts/${postId}`); }
async function createPost(boardId, title, content)        { return api('POST',   `/boards/${boardId}/posts`, { title, content }); }
async function updatePost(bId, pId, title, content)       { return api('PUT',    `/boards/${bId}/posts/${pId}`, { title, content }); }
async function deletePost(boardId, postId)                { return api('DELETE', `/boards/${boardId}/posts/${postId}`); }

/* ── 댓글 ── */
async function getCommentList(bId, pId, page = 1, size = 10) { return api('GET',  `/boards/${bId}/posts/${pId}/comments?page=${page}&size=${size}`); }
async function getComment(bId, pId, cId)                     { return api('GET',  `/boards/${bId}/posts/${pId}/comments/${cId}`); }
async function createComment(bId, pId, content)              { return api('POST', `/boards/${bId}/posts/${pId}/comments`, { content }); }
async function updateComment(bId, pId, cId, content)         { return api('PUT',  `/boards/${bId}/posts/${pId}/comments/${cId}`, { content }); }
async function deleteComment(bId, pId, cId)                  { return api('DELETE', `/boards/${bId}/posts/${pId}/comments/${cId}`); }

/* ── 쪽지 ── */
async function getSentMessages()                             { return api('GET',    '/message/sent'); }
async function getReceivedMessages()                         { return api('GET',    '/message/list'); }
async function sendMessage(receiverNickname, content)        { return api('POST',   '/message/send', { receiverNickname, content }); }
async function deleteSentMessage(id)                         { return api('DELETE', `/message/sent/${id}`); }
async function deleteReceivedMessage(id)                     { return api('DELETE', `/message/received/${id}`); }

/* ── 문의 & 신고 ── */
async function submitSupport(content)                        { return api('POST', '/message/support', { content }); }
async function submitReport(targetId, targetType, reason)    { return api('POST', '/api/reports', { targetId, targetType, reason }); }

/* ── 관리자 ── */
async function getDashboard()                                { return api('GET',   '/api/admin/dashboard/overview'); }
async function getAdminSupports()                            { return api('GET',   '/api/admin/dashboard/supports'); }
async function getAdminSupport(id)                           { return api('GET',   `/api/admin/dashboard/supports/${id}`); }
async function getAdminReports()                             { return api('GET',   '/api/admin/dashboard/reports'); }
async function getAdminReport(id)                            { return api('GET',   `/api/admin/dashboard/reports/${id}`); }
async function getUserList(page = 1, size = 10)              { return api('GET',   `/api/admin/dashboard/users?page=${page}&size=${size}`); }
async function getBoardManagers(boardId)                     { return api('GET',   `/boards/${boardId}/manager`); }
async function addBoardManager(boardId, userId)              { return api('POST',  `/boards/${boardId}/manager`, { id: userId }); }
async function removeBoardManager(boardId, userId)           { return api('DELETE',`/boards/${boardId}/manager`, { id: userId }); }
async function updateUserStatus(userId, newStatus)           { return api('PATCH', `/api/admin/dashboard/users/${userId}/status`, { newStatus }); }
async function searchUsers(keyword, page = 1, size = 10)     { return api('GET',   `/api/admin/dashboard/users/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`); }
