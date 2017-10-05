
Cumulo Editor
------

> A collabrative syntax tree editor of Clojure(Script).

* Editor UI http://cumulo-editor.cirru.org/
* [Keyboard Shortcuts](https://github.com/Cirru/cumulo-editor/wiki/Keyboard-Shortcuts)
* [Overview Video](https://www.youtube.com/watch?v=u5Eb_6KYGsA&t)

### Guide

Run editor:

```bash
npm i -g cumulo-editor
cumulo-editor
# open http://cumulo-editor.cirru.org/?port=6001
```

![Run in command line](https://pbs.twimg.com/media/DLSmv0cVwAEUCMi.png:large)
![Files browser](https://pbs.twimg.com/media/DLSnADUVYAAr43C.png:large)
![Expression editor](https://pbs.twimg.com/media/DLSnJ0FVAAA0Ehd.png:large)

### Options

To run editor with options:

```bash
port=6001 cumulo-editor
```

There are several options to configure:

* `port`, defaults to `6001`
* `output`, defaults to `src/`
* `extension`, defaults to `.cljs`

Those options are also stored in the snapshot file `coir.edn`.

To compile code at once from existing `coir.edn`:

```bash
op=compile cumulo-editor
```

The UI part takes options too:

```
http://cumulo-editor.cirru.org/?host=localhost&port=6001
```

* `port`, defaults to `6001`
* `host`, defaults to `localhost`

### Workflow

Based on https://github.com/Cumulo/cumulo-workflow

### License

MIT
