# 在 Winlator-GLIBC RootFS 中编译并集成 FEX 运行环境

这个文档是为当前仓库（`termux-glibc` 底层）准备的，目标是**先把 FEX 的 Linux 侧运行环境编出来并塞进 rootfs**，避免直接复用 bionic 产物导致 ABI 不匹配。

## 先明确：你现在要编的是什么

你提到的 CMOD/FEX 路线里，存在两类“FEX相关产物”：

1. **Linux 侧 FEX 运行时**（`FEXInterpreter` / `FEXServer` / `FEXLoader` 等）
   - 这是给 **glibc rootfs** 用的，必须按 glibc 目标编。
2. **Wine 侧 WoW64 桥接 DLL**（如 `libwow64fex.dll`）
   - 这是 ARM64EC Wine 里通过 `HODLL` 加载的内容包（WCP）层，不是 Linux ELF。

本次先解决第 1 类（你当前需求）。

---

## 我在仓库里加了一个可直接用的构建脚本

新增脚本：`scripts/build-fex-glibc-rootfs.sh`

它做的事：
- 拉取 `FEX-Emu/FEX`
- 切换指定分支/标签
- 初始化子模块
- 用 `clang + cmake + ninja` 进行 Release 构建
- 安装到 staging（`DESTDIR`）
- 打成 `tar.zst` 包
- （可选）直接 `rsync` 到你的 rootfs

---

## 最小使用方法

### 1) 只编译并打包（不落地 rootfs）

```bash
./scripts/build-fex-glibc-rootfs.sh \
  --workdir /tmp/fex-build \
  --ref main
```

产物在：
- `/tmp/fex-build/out/fex-glibc-main.tar.zst`

### 2) 编译后直接安装到 rootfs

```bash
./scripts/build-fex-glibc-rootfs.sh \
  --workdir /tmp/fex-build \
  --ref main \
  --rootfs /path/to/your/imagefs
```

---

## 推荐参数（给你这个场景）

如果你希望更稳定可复现：

```bash
./scripts/build-fex-glibc-rootfs.sh \
  --workdir /tmp/fex-build \
  --ref FEX-2508 \
  --clean \
  --rootfs /path/to/your/imagefs
```

- `--ref FEX-2508`：锁版本，方便回滚。
- `--clean`：每次干净构建，减少脏缓存问题。

---

## 依赖检查（脚本会自动校验）

需要命令：
- `git`
- `cmake`
- `ninja`
- `clang` / `clang++`
- `rsync`
- `tar` / `zstd`

缺哪个脚本会直接报错并退出。

---

## 集成后你要做的验证

在 rootfs 内至少验证：

1. 二进制是否存在
   - `/usr/bin/FEXInterpreter`
   - `/usr/bin/FEXServer`
2. 动态链接是否完整
   - 在 rootfs 环境执行 `ldd /usr/bin/FEXInterpreter`
3. 基础可运行性
   - 启动一次 `FEXInterpreter --help`（或最小 x86_64 测试程序）

如果这里通过，说明“glibc rootfs 的 FEX Linux runtime”已经到位。

---

## 你下一步（和 ARM64EC Wine 对接）

等 Linux 侧 runtime 跑通后，再做第二段：
- `libwow64fex.dll` 的 WCP 打包/注入
- `HODLL=libwow64fex.dll` 的容器启动链路切换

这一步是 Wine/容器层逻辑，不等于当前脚本处理的 ELF runtime。


---

## GitHub Actions 直接编译（已接入）

仓库已新增 workflow：`.github/workflows/build-fex-glibc.yml`

你可以在 GitHub 页面：
1. 进入 **Actions** → `build-fex-glibc`
2. 点击 **Run workflow**
3. 输入：
   - `fex_ref`：如 `main` 或 `FEX-2508`
   - `jobs`：并行编译线程数
4. 等待完成后，在该次运行的 **Artifacts** 下载 `fex-glibc-<ref>.tar.zst`

补充：该 workflow 会额外上传 `fex-glibc-build-logs`，即使构建失败也会有 `build.log` 和 CMake 日志可下载排错。

> 这个 workflow 运行在 `ubuntu-24.04-arm` runner，直接调用仓库内 `scripts/build-fex-glibc-rootfs.sh`。
