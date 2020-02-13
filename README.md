
Calcit Editor
------

> A collabrative syntax tree editor for Clojure(Script). [Introduction videos](https://www.youtube.com/watch?v=u5Eb_6KYGsA&t).

Main ideas:

* **Auto Layout**: expressions are layouted with CSS.
* **Tree Editing**: DOM-based tree editor, intuitive operations with shortcuts.
* **Call Stack Navigation**: fine-grained tabs by functions/definitions.
* **Collaborative**: changes synced to all connected clients in real time.

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

There are several options to configure in `:configs` field in `calcit.cirru`:

* `port`, defaults to `6001`
* `output`, defaults to `src/`
* `extension`, defaults to `.cljs`

Command options may help to compile code at once from existing `calcit.cirru`:

```bash
op=compile calcit-editor
```

The UI part takes several query options:

```
http://calcit-editor.cirru.org/?host=localhost&port=6001
```

* `port`, defaults to `6001`
* `host`, defaults to `localhost`

By default, Clojure(Script) code will be emitted in `src/`.
When server is stopped with `Ctrl c`, a `calcit.cirru` will be generated.

Set `local` to enable local version of web editor:

```bash
ui=local calcit-editor
# serving app at http://localhost:6101
```

### Workflow

Based on https://github.com/Cumulo/cumulo-workflow

### License

MIT
