# FEX glibc rootfs 构建说明（最小版）

为降低合并冲突，这里只保留和当前仓库直接相关的最小说明。

## 本仓库使用方式

### 1) 本地构建
```bash
./scripts/build-fex-glibc-rootfs.sh \
  --workdir /tmp/fex-build \
  --ref main \
  --clean
```

可选参数：
- `--enable-thunks`：开启 thunk 构建
- `--log-file <path>`：保存完整日志
- `--rootfs <dir>`：构建后直接安装到 rootfs

### 2) GitHub Actions 构建
工作流：`.github/workflows/build-fex-glibc.yml`

手动触发参数：
- `fex_ref`
- `jobs`
- `enable_thunks`

产物：
- 成功时：`fex-glibc-<ref>.tar.zst`
- 任意结果：`fex-glibc-build-logs`（含 `build.log` / CMake 日志）

## 目标

先确保 glibc rootfs 侧 FEX Linux 运行时可构建、可打包、可回放日志。
