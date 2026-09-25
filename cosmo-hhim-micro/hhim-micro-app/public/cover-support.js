// ============================================================
// 移动端 viewport 兼容脚本（由 index.html 内联脚本外链化而来）
// 外链化原因：CSP 收紧为 script-src 'self' 后，内联 <script>
// 会被浏览器拦截；外链同源脚本不受影响，行为完全一致。
// 注意：此脚本在 <head> 中同步执行（无 defer/async），
// document.write 输出 viewport meta，与内联时行为一致。
// ============================================================
var coverSupport =
  "CSS" in window &&
  typeof CSS.supports === "function" &&
  (CSS.supports("top: env(a)") || CSS.supports("top: constant(a)"));
document.write(
  '<meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0' +
    (coverSupport ? ", viewport-fit=cover" : "") +
    '" />'
);
