
Calcit Editor
------

> A collabrative syntax tree editor of Clojure(Script). [Overview Video](https://www.youtube.com/watch?v=u5Eb_6KYGsA&t).

Main ideas:

* **Tree Editing**: it's a DOM-based tree editor, NOT in raw text syntax.
* **Auto Layout**: expressions and rendered with CSS layouts.
* **Collaborative**: changes sync to all connected clients instantly.
* **Call Stack Navigation**: goto definition and back to preview editing location.

### Guide

![npm version of calcit-editor](https://img.shields.io/npm/v/calcit-editor.svg)

Install editor and try:

```bash
npm i -g calcit-editor
calcit-editor
# open http://calcit-editor.cirru.org/?port=6001
```

* [Editor UI](http://calcit-editor.cirru.org/)
* [Keyboard Shortcuts](https://github.com/Cirru/calcit-editor/wiki/Keyboard-Shortcuts)

![Run in command line](https://pbs.twimg.com/media/DLSmv0cVwAEUCMi.png:large)
![Files browser](https://pbs.twimg.com/media/DLSnADUVYAAr43C.png:large)
![Expression editor](https://pbs.twimg.com/media/DLSnJ0FVAAA0Ehd.png:large)

### Options

There are several options to configure in `:configs` field in `calcit.edn`:

* `port`, defaults to `6001`
* `output`, defaults to `src/`
* `extension`, defaults to `.cljs`

Command options may help to compile code at once from existing `calcit.edn`:

```bash
op=compile calcit-editor
```

The UI part takes options too:

```
http://calcit-editor.cirru.org/?host=localhost&port=6001
```

* `port`, defaults to `6001`
* `host`, defaults to `localhost`

By default, ClojureScript will be emitted in `src/`.
When server is stopped with `Ctrl c`, a `calcit.edn` will be generated.

Set `local` to enable local version of web editor:

```bash
ui=local calcit-editor
# serving app at http://localhost:6101
```

### Workflow

Based on https://github.com/Cumulo/cumulo-workflow

### License

MIT
