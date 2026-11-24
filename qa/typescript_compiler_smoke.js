/* Execute the exact official TypeScript compiler bundled inside the built APK.
 * No npm dependency, internet or Android emulator needed. This exercises the
 * very same transpileModule function/code path as ts-compiler.html.
 */
'use strict';
const assert = require('node:assert/strict');
const {execFileSync} = require('node:child_process');
const vm = require('node:vm');

const apk = process.argv[2];
if (!apk) throw new Error('Usage: node qa/typescript_compiler_smoke.js <engine-ts.apk>');
function asset(name) {
  return execFileSync('unzip', ['-p', apk, 'assets/' + name], {
    maxBuffer: 20 * 1024 * 1024
  }).toString('utf8');
}
const calls = [];
const sandbox = {
  window: {},
  CodeLabBridge: {complete: json => calls.push(JSON.parse(json))}
};
vm.createContext(sandbox);
vm.runInContext(asset('typescript.js'), sandbox, {timeout: 10000});
assert.match(sandbox.ts.version, /^5\.9\./, 'Unexpected TypeScript compiler version');
const html = asset('ts-compiler.html');
const inline = html.match(/<script>([\s\S]*?)<\/script>/);
assert.ok(inline, 'Embedded CodeLab TypeScript bridge is missing');
vm.runInContext(inline[1], sandbox, {timeout: 5000});
assert.equal(typeof sandbox.window.compileTs, 'function');

function execute(source, expected) {
  calls.length = 0;
  vm.runInContext('window.compileTs(' + JSON.stringify(source) + ')', sandbox, {
    timeout: 5000
  });
  assert.equal(calls.length, 1, 'TypeScript must deliver exactly one compiler reply');
  const result = calls[0];
  assert.ok(!result.error, 'Unexpected TypeScript compiler error: ' + result.error);
  assert.ok(result.js, 'TypeScript compiler produced empty JavaScript');
  const output = [];
  vm.runInNewContext(result.js, {
    console: {log: (...args) => output.push(args.join(' '))}
  }, {timeout: 2500});
  assert.equal(output.join('\n'), expected);
  return result.js;
}
const greeting = execute(
  "function greet(name: string): string { return 'Hello, ' + name + '!'; }\nconsole.log(greet('CodeLab'));",
  'Hello, CodeLab!'
);
assert.ok(!greeting.includes('name: string'), 'Type annotations must be erased');
execute("const answer: number = 6 * 7;\nconsole.log(answer);", '42');
calls.length = 0;
vm.runInContext("window.compileTs('const x: = 1;')", sandbox, {timeout: 5000});
assert.ok(calls[0] && calls[0].error, 'Invalid TypeScript must return an error');
console.log('PASS: packaged TypeScript compiler transpiles typed programs to runnable offline JavaScript and reports syntax errors');
