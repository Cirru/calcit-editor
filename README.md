
Cumulo Editor
------

> Refactoring Stack Editor to realtime collaborate.

Address http://cumulo-editor.cirru.org/

### Usage

```bash
npm i -g cumulo-editor
```

```bash
cumulo-editor
# or
port=6001 cumulo-editor
# then edit via a web app
open http://cumulo-editor.cirru.org/?port=6001
```

By default, ClojureScript will be emitted in `src/`.
When server is stopped with `Ctrl c`, a `coir.edn` will be generated.

```bash
# with an existing coir.edn
op=compile cumulo-editor
```

### Workflow

https://github.com/Cumulo/cumulo-workflow

### License

MIT
