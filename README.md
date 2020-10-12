
Calcit Editor
------

> Intuitive S-expressions editing for Clojure(Script).

* **Auto Layout**: expressions in blocks and inline-blocks, styled with CSS
* **Tree Editing**: intuitive way of structural editing as nested expressions
* **Call Stack Navigation**: more fine-grained functions navigation
* **Collaboration**: changes real-time synced among multiple clients via WebSockets

One function/definition in a screen, `Command d` to open called function at next tab, `Command j` `Command k` `Command i` to switch:

![Expression editor](https://pbs.twimg.com/media/ES6_JjPU4AEJ7zt?format=png&name=large)

Based on DOM/CSS, easy for another theme:

![Styling](https://pbs.twimg.com/media/ES6_PiQU4AM0ceN?format=png&name=large)

`Command p` to search and jump inspired by Sublime Text :

![Search panel](https://pbs.twimg.com/media/ES68XGoUwAAzudc?format=png&name=large)

Browse namespaces and functions/variables:

![Definitions browser](https://pbs.twimg.com/media/ES68ScLUEAAiW3Z?format=png&name=large)

### Usages

![npm CLI of calcit-editor](https://img.shields.io/npm/v/calcit-editor.svg)

Install CLI and start a local WebSocket server, it uses `calcit.cirru` as a snapshot file:

```bash
npm i -g calcit-editor
calcit-editor
```

UI of the editor is a webapp on http://calcit-editor.cirru.org/?port=6001

You may try with my project templates:

* simple virtual DOM playground [calcit-workflow](https://github.com/mvc-works/calcit-workflow)
* a toy Node.js script [calcit-nodejs-workflow](https://github.com/mvc-works/calcit-nodejs-workflow)

or even clone current repo for trying out.

Don't forget to check out [keyboard shortcuts](https://github.com/Cirru/calcit-editor/wiki/Keyboard-Shortcuts). My old [introduction videos](https://www.youtube.com/watch?v=u5Eb_6KYGsA&t) can be found on YouTube.

### Options

CLI variables for compiling code directly from `calcit.cirru`:

```bash
op=compile calcit-editor
```

The web UI takes several query options:

```
http://calcit-editor.cirru.org/?host=localhost&port=6001
```

* `port`, defaults to `6001`
* `host`, defaults to `localhost`, connects via WebSocket

By default, ClojureScript code is emitted in `src/` by pressing `Command s`.
When server is stopped with `Control c`, `calcit.cirru` is also updated.

There are also several options in `:configs` field in `calcit.cirru`:

* `port`, defaults to `6001`
* `output`, defaults to `src/`
* `extension`, defaults to `.cljs`

Editor UI is decoupled with WebSocket server, so it's okay to connect remote server from multiple pages with all expressions synced in real-time.

Also there's a local version of web editor to enable:

```bash
ui=local calcit-editor
# serving UI at http://localhost:6101
```

### Compact output

```bash
compact=true caclcit-editor
```

When `:compact-output? true` is specified in `calcit.cirru`, "Compact Mode" is activated. Clojure(Script) will no longer be emitted,
instead two files will be emitted:

* `compact.cirru` contains a compact version of data tree of the program.
* `.compact-inc.cirru` contains diff information from latest modification of per definition.

It's not useful for Clojure but would can be used for other experiments in [calcit-runner](https://github.com/Cirru/calcit-runner.nim).

### Workflow

Based on https://github.com/Cumulo/cumulo-workflow

### License

MIT
