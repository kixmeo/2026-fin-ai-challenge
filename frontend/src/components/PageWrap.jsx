import { C } from "../lib/theme.js";
function PageWrap({ children }) {
  return (
    <div style={{ minHeight: "100vh", background: C.surfaceDeep, color: C.text, fontFamily: "'Pretendard', -apple-system, BlinkMacSystemFont, 'Malgun Gothic', 'Apple SD Gothic Neo', sans-serif" }}>
      <style>{`
        * { box-sizing: border-box; }
        input, select, button { font-family: inherit; }
        input:focus, select:focus { border-color: ${C.primary} !important; }
        button:focus-visible, a:focus-visible, input:focus-visible { outline: 2px solid ${C.primary}; outline-offset: 2px; }
        ::-webkit-scrollbar { width: 8px; height: 8px; }
        ::-webkit-scrollbar-thumb { background: #D1D6DB; border-radius: 8px; }

        @keyframes spin { to { transform: rotate(360deg); } }
        @keyframes toastIn { from { opacity: 0; transform: translate(-50%, 10px); } to { opacity: 1; transform: translate(-50%, 0); } }
        @keyframes pageIn { from { opacity: 0; transform: translateY(18px); } to { opacity: 1; transform: translateY(0); } }
        @keyframes popIn { 0% { opacity: 0; transform: scale(0.86) translateY(16px); } 65% { opacity: 1; transform: scale(1.035) translateY(-3px); } 100% { opacity: 1; transform: scale(1) translateY(0); } }
        @keyframes blobFloat { 0%, 100% { transform: translate(0,0) scale(1); } 50% { transform: translate(18px,-16px) scale(1.14); } }
        @keyframes bob { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-9px); } }

        .page-enter { animation: pageIn .5s cubic-bezier(0.16,1,0.3,1) both; }
        .pop-in { animation: popIn .6s cubic-bezier(0.16,1,0.3,1) both; }
        .floaty { animation: bob 3.2s ease-in-out infinite; }

        .liftable { transition: transform .3s cubic-bezier(0.34,1.56,0.64,1), box-shadow .3s ease, border-color .3s ease; }
        .liftable:hover { transform: translateY(-7px) scale(1.014); box-shadow: 0 22px 40px rgba(20,24,32,0.11); border-color: ${C.primaryLight}; }

        .btn-pop { transition: transform .25s cubic-bezier(0.34,1.56,0.64,1), box-shadow .25s ease; }
        .btn-pop:hover { transform: translateY(-3px); box-shadow: 0 14px 30px rgba(49,130,246,0.28); }
        .btn-pop:active { transform: translateY(-1px) scale(0.97); }

        .blob { position: absolute; border-radius: 50%; filter: blur(48px); pointer-events: none; }

        .nav-item { position: relative; z-index: 1; display: flex; align-items: center; gap: 12px; padding: 0 16px; height: 48px; border-radius: 14px; cursor: pointer; border: none; background: none; width: 100%; text-align: left; font-size: 15.5px; font-weight: 700; color: ${C.textSub}; white-space: nowrap; transition: color .2s ease; }
        .nav-item:hover { color: ${C.text}; }
        .nav-item.active { color: ${C.primaryDark}; }
        .sidebar-pill { position: absolute; left: 0; right: 0; height: 48px; background: ${C.primaryLight}; border-radius: 14px; transition: transform .4s cubic-bezier(0.34,1.4,0.64,1); z-index: 0; }

        .icon-btn { width: 34px; height: 34px; border-radius: 10px; border: 1px solid ${C.border}; background: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; color: ${C.textSub}; transition: background .2s ease; }
        .icon-btn:hover { background: ${C.surfaceDeep}; }

        .fee-row { transition: background .15s ease; }
        .fee-row:hover { background: ${C.surfaceDeep}; }

        .grid-detail { display: grid; grid-template-columns: 1fr 320px; gap: 26px; align-items: start; }
        .grid-wage { display: grid; grid-template-columns: 420px 1fr; gap: 26px; align-items: start; }
        .grid-calendar { display: grid; grid-template-columns: 1fr 320px; gap: 26px; align-items: start; }

        @media (prefers-reduced-motion: reduce) {
          *, *::before, *::after { animation-duration: 0.01ms !important; animation-iteration-count: 1 !important; transition-duration: 0.01ms !important; }
        }

        @media (max-width: 900px) {
          .grid-detail, .grid-wage, .grid-calendar { grid-template-columns: 1fr; }
          .sidebar { width: 100% !important; flex-direction: row !important; align-items: center; overflow-x: auto; border-right: none !important; border-bottom: 1px solid ${C.border}; }
          .sidebar-nav { flex-direction: row !important; }
          .sidebar-pill { display: none; }
          .nav-item.active { background: ${C.primaryLight} !important; }
          .sidebar-foot { display: none !important; }
        }
      `}</style>
      {children}
    </div>
  );
}

export default PageWrap;
