/* =====================================================================
   console.js  —  fetch 인터셉터 / 카드 UI / localStorage 영속성
   ===================================================================== */
(function () {
    'use strict';

    const LS_LOGS    = 'imprint_logs_v3';
    const LS_BASEURL = 'imprint_baseurl';
    const MAX_LOGS   = 200;

    const $log    = document.getElementById('console-log');
    const $empty  = document.getElementById('console-empty');
    const $count  = document.getElementById('logCount');
    const $urlInp = document.getElementById('baseUrlInput');

    let currentFilter = 'all';

    /* ── localStorage ── */
    const LS = {
        loadLogs:  () => { try { return JSON.parse(localStorage.getItem(LS_LOGS) || '[]'); } catch(_){ return []; } },
        saveLogs:  (a) => { try { localStorage.setItem(LS_LOGS, JSON.stringify(a.slice(0, MAX_LOGS))); } catch(_){} },
        wipeLogs:  ()  => { try { localStorage.removeItem(LS_LOGS); } catch(_){} },
    };

    /* ── BASE URL ── */
    function initBaseUrl() {
        if (!$urlInp) return;
        const saved = localStorage.getItem(LS_BASEURL) || '';
        $urlInp.value = saved;
        if (typeof setBaseUrl === 'function') setBaseUrl(saved);
        $urlInp.addEventListener('input', () => {
            const v = $urlInp.value.trim();
            localStorage.setItem(LS_BASEURL, v);
            if (typeof setBaseUrl === 'function') setBaseUrl(v);
        });
    }

    /* ── Utilities ── */
    function ts() {
        const d = new Date();
        return d.toLocaleTimeString('ko-KR', { hour12: false }) + '.' + String(d.getMilliseconds()).padStart(3,'0');
    }
    function esc(s) {
        return String(s == null ? '' : s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
    }
    function dim(s) { return s == null ? '<span style="color:var(--fg-dim)">—</span>' : esc(s); }
    function dateStr(s) { return s ? esc(String(s).slice(0,10)) : ''; }
    function chip(text, cls) { return `<span class="chip chip-${cls}">${esc(text)}</span>`; }

    function highlight(obj) {
        const raw = typeof obj === 'string' ? obj : JSON.stringify(obj, null, 2);
        return raw.replace(
            /("(?:\\u[\da-fA-F]{4}|\\[^u]|[^\\"])*"(?:\s*:)?|\b(?:true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+-]?\d+)?)/g,
            m => {
                if (/^"/.test(m)) return /:$/.test(m) ? `<span class="json-key">${m}</span>` : `<span class="json-str">${m}</span>`;
                if (/true|false/.test(m)) return `<span class="json-bool">${m}</span>`;
                if (/null/.test(m))       return `<span class="json-null">${m}</span>`;
                return `<span class="json-num">${m}</span>`;
            }
        );
    }

    /* ── Type detection ── */
    function detectType(data, url) {
        if (!data || typeof data !== 'object') return 'raw';
        const u = url || '';
        if (!Array.isArray(data)) {
            if (data.totalUsers != null || data.totalBoards != null || data.totalPosts != null) return 'dashboard';
            if (data.email      != null && data.nickname != null)                               return 'user';
            if (data.title      != null && data.content != null && data.boardId != null)        return 'post';
            if (data.name       != null && (data.description != null || data.postCount != null))return 'board';
            if (data.content    != null && data.postId != null)                                 return 'comment';
            if ((data.senderNickname != null || data.receiverNickname != null) && data.content != null) return 'message';
            if (data.content && Array.isArray(data.content))                                    return detectListType(data.content, u);
        }
        if (Array.isArray(data)) return detectListType(data, u);
        return 'raw';
    }

    function detectListType(arr, url) {
        if (!arr || !arr.length) return 'list-empty';
        const f = arr[0]; if (!f || typeof f !== 'object') return 'list-raw';
        const u = url || '';
        if (f.email  != null && f.nickname != null)                             return 'list-user';
        if (f.title  != null && f.content  != null && f.boardId != null)        return 'list-post';
        if (f.name   != null && f.description != null)                          return 'list-board';
        if (f.content!= null && f.postId != null)                               return 'list-comment';
        if (f.senderNickname != null || f.receiverNickname != null)             return 'list-message';
        if (/\/users/.test(u))                                                  return 'list-user';
        if (/\/boards/.test(u) && !/posts/.test(u))                             return 'list-board';
        if (/\/posts/.test(u)  && !/comments/.test(u))                          return 'list-post';
        if (/\/comments/.test(u))                                               return 'list-comment';
        if (/\/message/.test(u))                                                return 'list-message';
        return 'list-raw';
    }

    /* ── Card renderers ── */

    function renderUser(u) {
        const initials = esc((u.nickname || u.name || u.email || '?').slice(0,2));
        return `<div class="rc user-card">
  <div class="user-card-top">
    <div class="user-avatar">${initials}</div>
    <div class="user-info">
      <div class="user-name">${dim(u.nickname || u.name)}</div>
      <div class="user-email">${dim(u.email)}</div>
    </div>
    <div style="display:flex;gap:4px;flex-shrink:0">
      ${u.role   ? chip(u.role,   u.role   === 'ADMIN'  ? 'purple':'blue')  : ''}
      ${u.status ? chip(u.status, u.status === 'BANNED' ? 'red'   :'green') : ''}
    </div>
  </div>
  <div class="user-card-meta">
    <div class="user-meta-cell"><div class="meta-label">ID</div><div class="meta-value">${dim(u.id)}</div></div>
    <div class="user-meta-cell"><div class="meta-label">실명</div><div class="meta-value">${dim(u.name)}</div></div>
    ${u.createdAt  ? `<div class="user-meta-cell"><div class="meta-label">가입일</div><div class="meta-value">${dateStr(u.createdAt)}</div></div>` : ''}
  </div>
</div>`;
    }

    function renderBoard(b) {
        const meta = [
            b.postCount    != null ? `게시물 ${esc(b.postCount)}` : '',
            b.managerCount != null ? `매니저 ${esc(b.managerCount)}` : '',
            b.createdAt            ? dateStr(b.createdAt) : '',
        ].filter(Boolean).join(' · ');
        return `<div class="rc board-card">
  <div class="board-card-header"><div class="board-title">${dim(b.name)}</div>${chip('#'+esc(b.id),'gray')}</div>
  ${b.description ? `<div class="board-desc">${esc(b.description)}</div>` : ''}
  ${meta          ? `<div class="board-footer">${meta}</div>` : ''}
</div>`;
    }

    function renderPost(p) {
        const meta = [
            p.authorNickname || p.author ? esc(p.authorNickname || p.author) : '',
            p.boardId != null ? `게시판 #${esc(p.boardId)}` : '',
            p.createdAt ? dateStr(p.createdAt) : '',
            p.commentCount != null ? `댓글 ${esc(p.commentCount)}` : '',
        ].filter(Boolean).join(' · ');
        return `<div class="rc post-card">
  <div class="post-card-top"><div class="post-title">${dim(p.title)}</div>${chip('#'+esc(p.id),'gray')}</div>
  ${p.content ? `<div class="post-content">${esc(p.content)}</div>` : ''}
  ${meta      ? `<div class="post-footer">${meta}</div>` : ''}
</div>`;
    }

    function renderComment(c) {
        return `<div class="rc comment-card">
  <div class="comment-card-top">
    ${chip('#'+esc(c.id),'gray')}
    <span class="comment-author">${dim(c.authorNickname || c.author)}</span>
    ${c.createdAt ? `<span class="comment-date">${dateStr(c.createdAt)}</span>` : ''}
  </div>
  <div class="comment-content">${esc(c.content)}</div>
</div>`;
    }

    function renderMessage(m) {
        return `<div class="rc message-card">
  <div class="message-card-top">
    <div class="message-parties">
      <span>${dim(m.senderNickname)}</span>
      <span class="message-arrow">→</span>
      <span>${dim(m.receiverNickname)}</span>
    </div>
    ${m.createdAt ? `<span class="message-date">${dateStr(m.createdAt)}</span>` : ''}
  </div>
  <div class="message-content">${esc(m.content)}</div>
</div>`;
    }

    function renderDashboard(d) {
        return `<div class="stat-grid">${
            Object.entries(d).map(([k,v]) =>
                `<div class="stat-card"><div class="stat-label">${esc(k)}</div><div class="stat-value">${esc(typeof v==='object'?JSON.stringify(v):v)}</div></div>`
            ).join('')
        }</div>`;
    }

    function renderErrorCard(data, status) {
        const msg = esc(data.message || data.error || JSON.stringify(data));
        const statusLabel = status ? `HTTP ${status}` : '오류';
        return `<div class="error-card">
  <div class="error-card-title">${statusLabel}</div>
  <div class="error-card-msg">${msg}</div>
</div>`;
    }

    /* ── List row renderers ── */
    function cardList(arr, fn) {
        if (!arr || !arr.length) return '<div class="msg-text">— 비어있음 —</div>';
        return `<div class="card-list">${arr.map(fn).join('')}</div>`;
    }

    function rowUser(u) {
        const initials = esc((u.nickname || u.name || u.email || '?').slice(0,2));
        return `<div class="rc row-card">
  <div class="user-avatar sm">${initials}</div>
  <div class="row-body"><div class="row-title">${dim(u.nickname)}</div><div class="row-sub">${dim(u.email)}</div></div>
  ${chip('#'+esc(u.id),'gray')}
  ${u.status ? chip(u.status, u.status==='BANNED'?'red':'green') : ''}
</div>`;
    }

    function rowBoard(b) {
        return `<div class="rc row-card">
  ${chip('#'+esc(b.id),'blue')}
  <div class="row-body"><div class="row-title">${dim(b.name)}</div>${b.description?`<div class="row-sub">${esc(b.description)}</div>`:''}</div>
  ${b.postCount != null ? chip(esc(b.postCount)+'개','gray') : ''}
</div>`;
    }

    function rowPost(p) {
        const meta = [
            p.authorNickname || p.author ? esc(p.authorNickname || p.author) : '',
            p.createdAt ? dateStr(p.createdAt) : '',
            p.commentCount != null ? `댓글 ${esc(p.commentCount)}` : '',
        ].filter(Boolean).join(' · ');
        return `<div class="rc row-card col">
  <div style="display:flex;align-items:center;gap:7px">${chip('#'+esc(p.id),'gray')}<span class="row-title">${dim(p.title)}</span></div>
  ${meta ? `<div class="row-sub">${meta}</div>` : ''}
</div>`;
    }

    function rowComment(c) {
        return `<div class="rc row-card col">
  <div style="display:flex;align-items:center;gap:7px">
    ${chip('#'+esc(c.id),'gray')}
    <span class="row-title">${dim(c.authorNickname || c.author)}</span>
    ${c.createdAt ? `<span class="row-sub" style="margin-left:auto">${dateStr(c.createdAt)}</span>` : ''}
  </div>
  <div class="row-sub" style="white-space:normal">${esc(c.content)}</div>
</div>`;
    }

    function rowMessage(m) {
        return `<div class="rc row-card">
  ${chip('#'+esc(m.id),'gray')}
  <div class="row-body">
    <div class="row-title">${dim(m.senderNickname)} <span style="color:var(--fg-dim);font-size:10px">→</span> ${dim(m.receiverNickname)}</div>
    <div class="row-sub clamp1">${esc(m.content)}</div>
  </div>
  ${m.createdAt ? `<span class="row-date">${dateStr(m.createdAt)}</span>` : ''}
</div>`;
    }

    /* ── Dispatcher ── */
    function renderUI(type, data, isSuccess, status) {
        if (!isSuccess) {
            if (data && (data.message || data.error)) return renderErrorCard(data, status);
            return null;
        }
        const arr = Array.isArray(data) ? data : (data && Array.isArray(data.content)) ? data.content : null;
        switch (type) {
            case 'user':         return renderUser(data);
            case 'board':        return renderBoard(data);
            case 'post':         return renderPost(data);
            case 'comment':      return renderComment(data);
            case 'message':      return renderMessage(data);
            case 'dashboard':    return renderDashboard(data);
            case 'list-user':    return cardList(arr, rowUser);
            case 'list-board':   return cardList(arr, rowBoard);
            case 'list-post':    return cardList(arr, rowPost);
            case 'list-comment': return cardList(arr, rowComment);
            case 'list-message': return cardList(arr, rowMessage);
            case 'list-empty':   return '<div class="msg-text">— 비어있음 —</div>';
            default:             return null;
        }
    }

    /* ── Entry body HTML ── */
    function buildBody(record) {
        const { data, isSuccess, url, dataType, status } = record;
        const type    = dataType || detectType(data, url);
        const uiHtml  = renderUI(type, data, isSuccess, status);
        const rawJson = data != null ? `<div class="json-block">${highlight(data)}</div>` : '<div class="msg-text">— 응답 없음 —</div>';

        if (uiHtml) {
            return `<div>
  <div class="res-tabs">
    <div class="res-tab active" data-tab="ui">UI</div>
    <div class="res-tab" data-tab="raw">Raw JSON</div>
  </div>
  <div class="res-panel active" data-panel="ui">${uiHtml}</div>
  <div class="res-panel" data-panel="raw">${rawJson}</div>
</div>`;
        }
        return data !== null && data !== undefined
            ? (typeof data === 'object' ? rawJson : `<div class="msg-text">${esc(String(data))}</div>`)
            : '<div class="msg-text">— 응답 없음 —</div>';
    }

    /* ── Copy to clipboard ── */
    function copyJson(id) {
        const rec = LS.loadLogs().find(r => r.id === id);
        if (!rec) return;
        const text = JSON.stringify(rec.data, null, 2);
        navigator.clipboard.writeText(text).then(() => {
            const btn = document.querySelector(`#${id} .log-copy`);
            if (btn) { btn.textContent = '✓'; setTimeout(() => { btn.textContent = 'copy'; }, 1200); }
        }).catch(() => {});
    }
    window.copyJson = copyJson;

    /* ── Delete single entry ── */
    window.deleteEntry = function(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
        LS.saveLogs(LS.loadLogs().filter(r => r.id !== id));
        syncUI();
    };

    /* ── Sync empty state + count badge ── */
    function syncUI() {
        const all     = $log.querySelectorAll('.log-entry');
        const visible = $log.querySelectorAll('.log-entry:not(.hidden)');
        $empty.style.display = visible.length ? 'none' : '';
        if ($count) {
            if (all.length) { $count.textContent = all.length; $count.classList.add('visible'); }
            else            { $count.classList.remove('visible'); }
        }
    }

    /* ── Mount entry to DOM ── */
    function mountEntry(record, prepend) {
        const body = buildBody(record);
        const el   = document.createElement('div');
        el.className    = `log-entry ${record.type} expanded`;
        el.id           = record.id;
        el.dataset.type = record.type;

        if (currentFilter !== 'all' && currentFilter !== record.type) el.classList.add('hidden');

        el.innerHTML = `
<div class="log-entry-header">
  <span class="log-status">${esc(record.statusText)}</span>
  <span class="log-method log-method-${(record.method||'').toLowerCase()}">${esc(record.method)}</span>
  <span class="log-url" title="${esc(record.url)}">${esc(record.url)}</span>
  <span class="log-time">${esc(record.time)}</span>
  <button class="log-copy" title="JSON 복사" onclick="event.stopPropagation();copyJson('${record.id}')">copy</button>
  <button class="log-delete" title="삭제" onclick="event.stopPropagation();deleteEntry('${record.id}')">
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
  </button>
  <svg class="log-chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
</div>
<div class="log-entry-body">${body}</div>`;

        el.querySelector('.log-entry-header').addEventListener('click', () => el.classList.toggle('expanded'));

        el.querySelectorAll('.res-tab').forEach(tab => {
            tab.addEventListener('click', e => {
                e.stopPropagation();
                el.querySelectorAll('.res-tab').forEach(t => t.classList.remove('active'));
                el.querySelectorAll('.res-panel').forEach(p => p.classList.remove('active'));
                tab.classList.add('active');
                el.querySelector(`.res-panel[data-panel="${tab.dataset.tab}"]`).classList.add('active');
            });
        });

        if (prepend) $log.insertBefore(el, $log.firstChild);
        else         $log.insertBefore(el, $empty);
    }

    /* ── Add new entry ── */
    function addEntry(opts) {
        const type       = opts.isSuccess ? 'success' : 'error';
        const statusText = String(opts.status || (opts.isSuccess ? 'OK' : 'ERR'));
        const id         = 'e' + Date.now() + Math.random().toString(36).slice(2,5);
        const record = {
            id, type, statusText,
            method:    opts.method,
            url:       opts.url,
            data:      opts.data,
            status:    opts.status,
            isSuccess: opts.isSuccess,
            time:      ts(),
            dataType:  detectType(opts.data, opts.url),
        };
        mountEntry(record, true);
        syncUI();
        const logs = LS.loadLogs();
        logs.unshift(record);
        LS.saveLogs(logs);
    }

    /* ── Restore ── */
    function restoreLogs() {
        const logs = LS.loadLogs();
        if (!logs.length) return;
        [...logs].forEach(r => mountEntry(r, false));
        syncUI();
    }

    /* ── Fetch interceptor ── */
    const _orig = window.fetch.bind(window);
    window.fetch = async function(input, init) {
        const method  = ((init && init.method) || 'GET').toUpperCase();
        const fullUrl = typeof input === 'string' ? input : input.url;
        // Strip both origin AND BASE_URL prefix
        const baseUrl = (typeof BASE_URL !== 'undefined' ? BASE_URL : '') || '';
        let shortUrl  = fullUrl;
        if (baseUrl && fullUrl.startsWith(baseUrl)) shortUrl = fullUrl.slice(baseUrl.length);
        else shortUrl = fullUrl.replace(window.location.origin, '');
        if (!shortUrl) shortUrl = fullUrl;

        try {
            const res = await _orig(input, init);
            let data = null;
            try { data = await res.clone().json(); } catch(_) {}
            addEntry({ status: res.status, method, url: shortUrl, data, isSuccess: res.ok });
            return res;
        } catch(err) {
            addEntry({ status: 'NET', method, url: shortUrl, data: { error: err.message }, isSuccess: false });
            throw err;
        }
    };

    /* ── Filters ── */
    function applyFilter(filter) {
        currentFilter = filter;
        document.querySelectorAll('.con-filter').forEach(b => b.classList.toggle('active', b.dataset.filter === filter));
        $log.querySelectorAll('.log-entry').forEach(el => {
            el.classList.toggle('hidden', filter !== 'all' && el.dataset.type !== filter);
        });
        syncUI();
    }
    document.querySelectorAll('.con-filter').forEach(b => b.addEventListener('click', () => applyFilter(b.dataset.filter)));

    /* ── Collapse all / expand all ── */
    window.collapseAll = function() { $log.querySelectorAll('.log-entry').forEach(e => e.classList.remove('expanded')); };

    /* ── Buttons ── */
    document.getElementById('clearConsole').addEventListener('click', () => {
        $log.querySelectorAll('.log-entry').forEach(e => e.remove());
        syncUI();
    });
    document.getElementById('resetConsole').addEventListener('click', () => {
        if (!confirm('로그를 전부 삭제할까요? (localStorage 포함)')) return;
        $log.querySelectorAll('.log-entry').forEach(e => e.remove());
        LS.wipeLogs();
        syncUI();
    });

    /* ── Keyboard shortcuts ── */
    document.addEventListener('keydown', e => {
        // S = sidebar toggle (not in input)
        if (e.key === 's' && !e.ctrlKey && !e.metaKey && !e.altKey) {
            if (!['INPUT','TEXTAREA','SELECT'].includes(document.activeElement?.tagName)) {
                document.getElementById('sidebarToggle')?.click();
            }
        }
        // Escape = collapse all console entries
        if (e.key === 'Escape' && !e.ctrlKey) {
            collapseAll();
        }
    });

    /* ── Init ── */
    initBaseUrl();
    restoreLogs();
})();
