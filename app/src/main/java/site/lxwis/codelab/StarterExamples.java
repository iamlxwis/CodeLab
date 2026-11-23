package site.lxwis.codelab;

/**
 * Starter source for every catalog entry. This describes file creation only:
 * providing an example does not imply that an offline runtime is bundled.
 */
public final class StarterExamples {
    public static final String BEGINNER = "Beginner";
    public static final String HELLO = "Hello";
    public static final String BLANK = "Blank";

    public static boolean validMode(String mode) {
        return BEGINNER.equals(mode) || HELLO.equals(mode) || BLANK.equals(mode);
    }

    public static String fileName(String language, String extension) {
        return switch (language) {
            case "Dockerfile", "Makefile" -> language;
            case "HTML", "Web" -> "index.html";
            case "Java", "Kotlin", "Haskell", "Scala" -> "Main." + extension;
            case "C#" -> "Program.cs";
            default -> "main." + extension;
        };
    }

    public static String example(String language, String mode) {
        if (!validMode(mode)) throw new IllegalArgumentException("Unknown starter mode: " + mode);
        if (BLANK.equals(mode)) return "";
        if (HELLO.equals(mode)) {
            String hello = switch (language) {
            case "JavaScript" -> "print(\"Hello, CodeLab!\");\n";
            case "Python" -> "print(\"Hello, CodeLab!\")\n";
            case "TypeScript" -> "console.log(\"Hello, CodeLab!\");\n";
            case "Lua" -> "print(\"Hello, CodeLab!\")\n";
            case "SQLite" -> "SELECT 'Hello, CodeLab!' AS greeting;\n";
            case "Java" -> "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, CodeLab!\");\n    }\n}\n";
            case "Kotlin" -> "fun main() {\n    println(\"Hello, CodeLab!\")\n}\n";
                default -> null;
            };
            if (hello != null) return hello;
        }
        return switch (language) {
            case "JavaScript" -> "function greet(name) {\n  return \"Hello, \" + name + \"!\";\n}\nprint(greet(\"CodeLab\"));\n";
            case "Lua" -> "local greet = function(name)\n  return \"Hello, \" .. name .. \"!\"\nend\nprint(greet(\"CodeLab\"))\n";
            case "Java" -> "public class Main {\n    public static void main(String[] args) {\n        int total = 0;\n        for (int i = 1; i <= 3; i++) total += i;\n        System.out.println(\"Hello, CodeLab! \" + total);\n    }\n}\n";
            case "SQLite" -> "CREATE TABLE greetings (id INTEGER PRIMARY KEY, message TEXT);\nINSERT INTO greetings(message) VALUES ('Hello, CodeLab!');\nSELECT * FROM greetings;\n";
            case "HTML" -> "<!doctype html>\n<html lang=\"en\">\n<head><meta charset=\"utf-8\"><title>CodeLab</title></head>\n<body><h1>Hello, CodeLab!</h1></body>\n</html>\n";
            case "CSS" -> "body {\n  font-family: system-ui, sans-serif;\n  background: #101517;\n  color: #e6eeed;\n}\n";
            case "Python" -> "def greet(name):\n    return f\"Hello, {name}!\"\n\nprint(greet(\"CodeLab\"))\n";
            case "C" -> "#include <stdio.h>\nint main(void) {\n    puts(\"Hello, CodeLab!\");\n    return 0;\n}\n";
            case "C++" -> "#include <iostream>\nint main() {\n    std::cout << \"Hello, CodeLab!\\n\";\n    return 0;\n}\n";
            case "C#" -> "using System;\nclass Program {\n    static void Main() { Console.WriteLine(\"Hello, CodeLab!\"); }\n}\n";
            case "TypeScript" -> "function greet(name: string): string {\n  return \"Hello, \" + name + \"!\";\n}\nconsole.log(greet(\"CodeLab\"));\n";
            case "Kotlin" -> "fun main() {\n    val name = \"CodeLab\"\n    println(\"Hello, $name!\")\n}\n";
            case "Rust" -> "fn main() {\n    let name = \"CodeLab\";\n    println!(\"Hello, {name}!\");\n}\n";
            case "Go" -> "package main\nimport \"fmt\"\nfunc main() { fmt.Println(\"Hello, CodeLab!\") }\n";
            case "PHP" -> "<?php\n$name = \"CodeLab\";\necho \"Hello, $name!\\n\";\n";
            case "Ruby" -> "name = \"CodeLab\"\nputs \"Hello, #{name}!\"\n";
            case "Swift" -> "let name = \"CodeLab\"\nprint(\"Hello, \\(name)!\")\n";
            case "Bash" -> "#!/usr/bin/env bash\nname=\"CodeLab\"\necho \"Hello, $name!\"\n";
            case "Dart" -> "void main() {\n  final name = \"CodeLab\";\n  print(\"Hello, $name!\");\n}\n";
            case "Scala" -> "object Main extends App {\n  val name = \"CodeLab\"\n  println(s\"Hello, $name!\")\n}\n";
            case "Perl" -> "use strict;\nuse warnings;\nmy $name = \"CodeLab\";\nprint \"Hello, $name!\\n\";\n";
            case "Haskell" -> "main :: IO ()\nmain = putStrLn \"Hello, CodeLab!\"\n";
            case "R" -> "name <- \"CodeLab\"\ncat(paste(\"Hello,\", name, \"!\\n\"))\n";
            case "Elixir" -> "name = \"CodeLab\"\nIO.puts(\"Hello, #{name}!\")\n";
            case "Erlang" -> "-module(main).\n-export([main/0]).\nmain() -> io:format(\"Hello, CodeLab!~n\").\n";
            case "OCaml" -> "let () = print_endline \"Hello, CodeLab!\"\n";
            case "Fortran" -> "program main\n  print *, \"Hello, CodeLab!\"\nend program main\n";
            case "COBOL" -> "       IDENTIFICATION DIVISION.\n       PROGRAM-ID. HELLO.\n       PROCEDURE DIVISION.\n           DISPLAY \"Hello, CodeLab!\".\n           STOP RUN.\n";
            case "Pascal" -> "program Main;\nbegin\n  writeln('Hello, CodeLab!');\nend.\n";
            case "Assembly" -> "; Target-specific assembly: select a CPU and assembler before compiling.\n; Example x86-64 instruction:\nmov rax, 1\n";
            case "Objective-C" -> "#import <Foundation/Foundation.h>\nint main(void) {\n    @autoreleasepool { NSLog(@\"Hello, CodeLab!\"); }\n    return 0;\n}\n";
            case "Groovy" -> "def name = \"CodeLab\"\nprintln \"Hello, $name!\"\n";
            case "F#" -> "printfn \"Hello, CodeLab!\"\n";
            case "Visual Basic .NET" -> "Module Program\n    Sub Main()\n        Console.WriteLine(\"Hello, CodeLab!\")\n    End Sub\nEnd Module\n";
            case "Prolog" -> "hello :- write(\"Hello, CodeLab!\"), nl.\n";
            case "Julia" -> "name = \"CodeLab\"\nprintln(\"Hello, $name!\")\n";
            case "Zig" -> "const std = @import(\"std\");\npub fn main() void {\n    std.debug.print(\"Hello, CodeLab!\\n\", .{});\n}\n";
            case "Nim" -> "let name = \"CodeLab\"\necho \"Hello, \", name, \"!\"\n";
            case "Crystal" -> "name = \"CodeLab\"\nputs \"Hello, #{name}!\"\n";
            case "D" -> "import std.stdio;\nvoid main() { writeln(\"Hello, CodeLab!\"); }\n";
            case "Ada" -> "with Ada.Text_IO; use Ada.Text_IO;\nprocedure Main is\nbegin\n   Put_Line(\"Hello, CodeLab!\");\nend Main;\n";
            case "Common Lisp" -> "(format t \"Hello, CodeLab!~%\")\n";
            case "Scheme" -> "(display \"Hello, CodeLab!\")\n(newline)\n";
            case "Clojure" -> "(println \"Hello, CodeLab!\")\n";
            case "ClojureScript" -> "(.log js/console \"Hello, CodeLab!\")\n";
            case "Elm" -> "module Main exposing (main)\nimport Html exposing (text)\nmain = text \"Hello, CodeLab!\"\n";
            case "PureScript" -> "module Main where\nimport Prelude\nimport Effect (Effect)\nimport Effect.Console (log)\nmain :: Effect Unit\nmain = log \"Hello, CodeLab!\"\n";
            case "ReasonML" -> "Js.log(\"Hello, CodeLab!\");\n";
            case "ReScript" -> "Js.log(\"Hello, CodeLab!\")\n";
            case "PowerShell" -> "$name = \"CodeLab\"\nWrite-Output \"Hello, $name!\"\n";
            case "Fish" -> "set name CodeLab\necho \"Hello, $name!\"\n";
            case "Zsh" -> "#!/usr/bin/env zsh\nname=\"CodeLab\"\necho \"Hello, $name!\"\n";
            case "Tcl" -> "set name \"CodeLab\"\nputs \"Hello, $name!\"\n";
            case "AWK" -> "BEGIN { print \"Hello, CodeLab!\" }\n";
            case "Solidity" -> "// SPDX-License-Identifier: MIT\npragma solidity ^0.8.0;\ncontract Greeting {\n    string public message = \"Hello, CodeLab!\";\n}\n";
            case "Move" -> "module 0x1::greeting {\n    public fun hello(): u64 { 1 }\n}\n";
            case "VHDL" -> "library IEEE;\nuse IEEE.STD_LOGIC_1164.ALL;\nentity greeting is end greeting;\narchitecture behavior of greeting is begin end behavior;\n";
            case "Verilog" -> "module greeting;\n  initial begin $display(\"Hello, CodeLab!\"); $finish; end\nendmodule\n";
            case "SystemVerilog" -> "module greeting;\n  initial begin $display(\"Hello, CodeLab!\"); $finish; end\nendmodule\n";
            case "CUDA" -> "#include <stdio.h>\n__global__ void hello() { printf(\"Hello, CodeLab!\\n\"); }\nint main() { hello<<<1, 1>>>(); return 0; }\n";
            case "GLSL" -> "#version 300 es\nprecision mediump float;\nout vec4 fragColor;\nvoid main() { fragColor = vec4(0.2, 0.8, 0.7, 1.0); }\n";
            case "HLSL" -> "float4 main() : SV_Target { return float4(0.2, 0.8, 0.7, 1.0); }\n";
            case "GraphQL" -> "query HelloCodeLab {\n  __typename\n}\n";
            case "LaTeX" -> "\\documentclass{article}\n\\begin{document}\nHello, CodeLab!\n\\end{document}\n";
            case "Nix" -> "{ greeting = \"Hello, CodeLab!\"; }\n";
            case "HCL" -> "locals {\n  greeting = \"Hello, CodeLab!\"\n}\n";
            case "YAML" -> "app: CodeLab\nmessage: Hello, CodeLab!\noffline: true\n";
            case "TOML" -> "app = \"CodeLab\"\nmessage = \"Hello, CodeLab!\"\noffline = true\n";
            case "JSON" -> "{\n  \"app\": \"CodeLab\",\n  \"message\": \"Hello, CodeLab!\",\n  \"offline\": true\n}\n";
            case "XML" -> "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<app name=\"CodeLab\"><message>Hello, CodeLab!</message></app>\n";
            case "Markdown" -> "# Hello, CodeLab!\n\nStart writing in **Markdown**.\n";
            case "Sass" -> "$accent: #83e5ca\nbody\n  color: $accent\n";
            case "SCSS" -> "$accent: #83e5ca;\nbody { color: $accent; }\n";
            case "Less" -> "@accent: #83e5ca;\nbody { color: @accent; }\n";
            case "Svelte" -> "<script>\n  let name = \"CodeLab\";\n</script>\n<h1>Hello, {name}!</h1>\n";
            case "Vue" -> "<template>\n  <h1>Hello, CodeLab!</h1>\n</template>\n";
            case "Astro" -> "---\nconst name = \"CodeLab\";\n---\n<h1>Hello, {name}!</h1>\n";
            case "JSX" -> "export default function App() {\n  return <h1>Hello, CodeLab!</h1>;\n}\n";
            case "TSX" -> "export default function App(): JSX.Element {\n  return <h1>Hello, CodeLab!</h1>;\n}\n";
            case "Pug" -> "doctype html\nhtml\n  head\n    title CodeLab\n  body\n    h1 Hello, CodeLab!\n";
            case "Handlebars" -> "<h1>Hello, {{name}}!</h1>\n";
            case "Dockerfile" -> "FROM alpine:latest\nCMD [\"echo\", \"Hello, CodeLab!\"]\n";
            case "Makefile" -> "all:\n\t@echo Hello, CodeLab!\n";
            case "INI" -> "[app]\nname = CodeLab\nmessage = Hello, CodeLab!\n";
            case "BASIC" -> "10 PRINT \"Hello, CodeLab!\"\n20 END\n";
            case "Smalltalk" -> "Transcript show: 'Hello, CodeLab!'; cr.\n";
            case "APL" -> "⎕←'Hello, CodeLab!'\n";
            case "Forth" -> ".\" Hello, CodeLab!\" CR\n";
            case "Wolfram Language" -> "Print[\"Hello, CodeLab!\"]\n";
            case "Idris" -> "main : IO ()\nmain = putStrLn \"Hello, CodeLab!\"\n";
            case "Agda" -> "module Main where\n-- Add an Agda definition here.\n";
            case "Lean" -> "def main : IO Unit :=\n  IO.println \"Hello, CodeLab!\"\n";
            case "Pony" -> "actor Main\n  new create(env: Env) =>\n    env.out.print(\"Hello, CodeLab!\")\n";
            case "GDScript" -> "extends Node\nfunc _ready():\n    print(\"Hello, CodeLab!\")\n";
            case "Q#" -> "namespace CodeLab {\n    open Microsoft.Quantum.Diagnostics;\n    operation Main() : Unit { Message(\"Hello, CodeLab!\"); }\n}\n";
            default -> throw new IllegalArgumentException("No starter example for: " + language);
        };
    }

    private StarterExamples() {}
}
