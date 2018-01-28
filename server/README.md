
Command line for Calcit Editor
---

### Usage

```bash
npm i -g calcit-editor
```

```bash
calcit-editor
# or
port=6001 calcit-editor
# then edit via a web app
open http://calcit-editor.cirru.org/?port=6001
```

By default, ClojureScript will be emitted in `src/`.
When server is stopped with `Ctrl c`, a `calcit.edn` will be generated.

```bash
# with an existing calcit.edn
op=compile calcit-editor
```

### More

https://github.com/Cirru/calcit-editor

### License

MIT
