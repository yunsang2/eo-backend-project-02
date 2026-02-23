/* =====================================================================
   controller.js
   ===================================================================== */
(function () {
    'use strict';

    const params     = new URLSearchParams(window.location.search);
    const formNum    = parseInt(params.get('form')) || 1;
    const tokenParam = params.get('token') || '';

    /* ── Show active form ── */
    const $form = document.querySelector(`#content > div[data-form="${formNum}"]`);
    if ($form) $form.classList.add('show');

    /* ── Header title ── */
    const $title = document.getElementById('headerTitle');
    if ($title && $form) $title.textContent = $form.dataset.title || '';

    /* ── Active nav link ── */
    document.querySelectorAll('#sidebar a').forEach(a => {
        try {
            if (new URL(a.href, location.origin).searchParams.get('form') === String(formNum)) {
                a.classList.add('active');

                const savedScroll = localStorage.getItem('sidebarScroll');
                if (savedScroll) {
                    setTimeout(() => {
                        $sidebar.scrollTo(0, parseInt(savedScroll, 10));
                    }, 0);
                }
            }
        } catch(_) {}
    });

    /* ── Sidebar toggle (state persisted) ── */
    const $sidebar   = document.getElementById('sidebar');
    const LS_SIDEBAR = 'imprint_sidebar';

    function setSidebar(collapsed) {
        $sidebar.classList.toggle('collapsed', collapsed);
        localStorage.setItem(LS_SIDEBAR, collapsed ? '1' : '0');
    }
    if (localStorage.getItem(LS_SIDEBAR) === '1') $sidebar.classList.add('collapsed');
    document.getElementById('sidebarToggle')?.addEventListener('click', () => {
        setSidebar(!$sidebar.classList.contains('collapsed'));
    });

    /* ── Field memory ── */
    // Key = "field_{name}" globally (폼 공통, 같은 name은 같은 값)
    const LS_FIELDS = 'imprint_fields_v2';
    function loadFields() { try { return JSON.parse(localStorage.getItem(LS_FIELDS) || '{}'); } catch(_){ return {}; } }
    function saveField(name, val) {
        const m = loadFields(); m[name] = val;
        try { localStorage.setItem(LS_FIELDS, JSON.stringify(m)); } catch(_){}
    }

    // Restore + listen only for non-password, non-submit, non-default-value inputs
    document.querySelectorAll('#content input:not([type="password"]):not([type="submit"]):not([type="button"])').forEach(inp => {
        if (!inp.name || inp.hasAttribute('value')) return; // skip number inputs with preset defaults
        const saved = loadFields()[inp.name];
        if (saved != null) inp.value = saved;
        inp.addEventListener('change', () => saveField(inp.name, inp.value));
    });

    /* ── Ctrl+Enter → submit ── */
    document.addEventListener('keydown', e => {
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            const f = document.activeElement?.closest('form');
            if (f) { e.preventDefault(); f.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true })); }
        }
    });

    /* ── Token pre-fill ── */
    if (tokenParam) {
        document.querySelectorAll('input[name="token"]').forEach(inp => {
            inp.value = tokenParam;
            inp.readOnly = true;
            inp.style.color = 'var(--fg-dim)';
            inp.title = 'URL 파라미터에서 자동 주입됨';
        });
    }

    /* ── RUN button loading state ── */
    function setLoading(form, loading) {
        const btn = form.querySelector('.run-btn');
        if (!btn) return;
        btn.disabled   = loading;
        btn.value      = loading ? '…' : '▶  RUN';
        btn.style.opacity = loading ? '0.5' : '';
    }

    /* ── Helper ── */
    function F(n) { return document.querySelectorAll(`#content > div[data-form="${n}"] form`); }

    function bind(form, handler) {
        if (!form) return;
        form.addEventListener('submit', async e => {
            e.preventDefault();
            setLoading(form, true);
            try { await handler(e.target); } catch(_) {}
            finally { setLoading(form, false); }
        });
    }

    /* ── Form bindings ── */
    const f1 = F(1);
    bind(f1[0], t => emailSend(t.email.value));
    bind(f1[1], t => emailVerify(t.email.value, t.code.value));
    bind(f1[2], t => signup(t.email.value, t.password.value, t.nickname.value, t.name.value));

    bind(F(2)[0], t => login(t.email.value, t.password.value));
    bind(F(3)[0], ()=> info());
    bind(F(4)[0], t => updateInfo(t.nickname.value, t.name.value));

    const f5 = F(5);
    bind(f5[0], t => sendResetPassword(t.email.value));
    bind(f5[1], t => resetPassword(t.token.value, t.newPassword.value));

    bind(F(6)[0], ()=> logout());
    bind(F(7)[0], ()=> { if (confirm('정말 회원탈퇴 하시겠습니까?\n이 작업은 되돌릴 수 없습니다.')) return leave(); });

    const f8 = F(8);
    bind(f8[0], t => getBoardList(t.page.value || 1, t.size.value || 10));
    bind(f8[1], t => getBoard(t.id.value));

    bind(F(9)[0],  t => createBoard(t.name.value, t.description.value));
    bind(F(10)[0], t => updateBoard(t.id.value, t.name.value, t.description.value));
    bind(F(11)[0], t => deleteBoard(t.id.value));

    const f12 = F(12);
    bind(f12[0], t => getPostList(t.boardId.value, t.page.value || 1, t.size.value || 10));
    bind(f12[1], t => getPost(t.boardId.value, t.postId.value));

    bind(F(13)[0], t => createPost(t.boardId.value, t.title.value, t.content.value));
    bind(F(14)[0], t => updatePost(t.boardId.value, t.postId.value, t.title.value, t.content.value));
    bind(F(15)[0], t => deletePost(t.boardId.value, t.postId.value));

    const f16 = F(16);
    bind(f16[0], t => getCommentList(t.boardId.value, t.postId.value, t.page.value || 1, t.size.value || 10));
    bind(f16[1], t => getComment(t.boardId.value, t.postId.value, t.commentId.value));

    bind(F(17)[0], t => createComment(t.boardId.value, t.postId.value, t.content.value));
    bind(F(18)[0], t => updateComment(t.boardId.value, t.postId.value, t.commentId.value, t.content.value));
    bind(F(19)[0], t => deleteComment(t.boardId.value, t.postId.value, t.commentId.value));

    bind(F(20)[0], ()=> getSentMessages());
    bind(F(21)[0], ()=> getReceivedMessages());
    bind(F(22)[0], t => sendMessage(t.receiverNickname.value, t.content.value));
    bind(F(23)[0], t => deleteSentMessage(t.id.value));
    bind(F(24)[0], t => deleteReceivedMessage(t.id.value));

    bind(F(25)[0], t => submitSupport(t.content.value));
    bind(F(26)[0], t => submitReport(t.targetId.value, t.targetType.value, t.reason.value));

    bind(F(27)[0], ()=> getDashboard());

    const f28 = F(28);
    bind(f28[0], ()=> getAdminSupports());
    bind(f28[1], t => getAdminSupport(t.supportId.value));

    const f29 = F(29);
    bind(f29[0], ()=> getAdminReports());
    bind(f29[1], t => getAdminReport(t.reportId.value));

    bind(F(30)[0], t => getUserList(t.page.value || 1, t.size.value || 10));

    const f31 = F(31);
    bind(f31[0], t => getBoardManagers(t.boardId.value));
    bind(f31[1], t => addBoardManager(t.boardId.value, t.userId.value));
    bind(f31[2], t => removeBoardManager(t.boardId.value, t.userId.value));

    bind(F(32)[0], t => updateUserStatus(t.userId.value, t.newStatus.value));
    bind(F(33)[0], t => searchUsers(t.keyword.value, t.page.value || 1, t.size.value || 10));

    $sidebar.addEventListener('scroll', (event) => {
        localStorage.setItem('sidebarScroll', JSON.stringify($sidebar.scrollTop));
    });
})();
