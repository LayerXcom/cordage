# Cordage documentation generator
![Publish GitHub Pages](https://github.com/LayerXcom/cordage/workflows/Publish%20GitHub%20Pages/badge.svg)

To generate the site, Hugo and Docsy theme are used.

- [What is Hugo](https://gohugo.io/about/what-is-hugo/)
- [What is Docsy](https://www.docsy.dev/docs/)


## Setup generator
1. [Install Hugo](https://gohugo.io/getting-started/installing/)
2. Install dependencies

```
yarn
git submodule update --recursive --init
```

Update dependencies
```
yarn
git submodule update --recursive
```

Update docsy version
```
git submodule update --recursive --remote
```

## Running the website locally

```
hugo server
```
