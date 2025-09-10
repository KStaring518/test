# Ollama API 使用指南

## 概述
Ollama 是一个本地大语言模型运行框架，支持多种开源模型。本文档介绍如何在我们的智能客服系统中正确使用 Ollama API。

## 安装 Ollama

### Windows
1. 访问 [Ollama官网](https://ollama.ai/download)
2. 下载 Windows 版本安装包
3. 运行安装程序
4. 打开命令行，下载模型：
```bash
ollama pull qwen2.5:7b
```

### macOS
```bash
brew install ollama
ollama pull qwen2.5:7b
```

### Linux
```bash
curl -fsSL https://ollama.ai/install.sh | sh
ollama pull qwen2.5:7b
```

## 启动 Ollama 服务
```bash
# 启动服务
ollama serve

# 测试模型
ollama run qwen2.5:7b
```

## API 端点

### 1. Chat API (推荐)
**端点**: `POST /api/chat`

**请求格式**:
```json
{
  "model": "qwen2.5:7b",
  "messages": [
    {
      "role": "system",
      "content": "你是一个专业的零食商城客服助手..."
    },
    {
      "role": "user", 
      "content": "如何查询订单状态？"
    }
  ],
  "stream": false,
  "options": {
    "temperature": 0.7,
    "num_predict": 2048
  }
}
```

**响应格式**:
```json
{
  "model": "qwen2.5:7b",
  "created_at": "2024-01-01T00:00:00Z",
  "message": {
    "role": "assistant",
    "content": "您可以通过以下方式查询订单状态..."
  },
  "done": true,
  "total_duration": 1234567890,
  "load_duration": 123456789,
  "prompt_eval_count": 50,
  "prompt_eval_duration": 123456789,
  "eval_count": 100,
  "eval_duration": 123456789
}
```

### 2. Generate API (旧版本)
**端点**: `POST /api/generate`

**请求格式**:
```json
{
  "model": "qwen2.5:7b",
  "prompt": "你是一个专业的零食商城客服助手。用户问：如何查询订单状态？",
  "stream": false
}
```

**响应格式**:
```json
{
  "model": "qwen2.5:7b",
  "created_at": "2024-01-01T00:00:00Z",
  "response": "您可以通过以下方式查询订单状态...",
  "done": true,
  "context": [1, 2, 3],
  "total_duration": 1234567890,
  "load_duration": 123456789,
  "prompt_eval_count": 50,
  "prompt_eval_duration": 123456789,
  "eval_count": 100,
  "eval_duration": 123456789
}
```

### 3. 模型列表
**端点**: `GET /api/tags`

**响应格式**:
```json
{
  "models": [
    {
      "name": "qwen2.5:7b",
      "modified_at": "2024-01-01T00:00:00Z",
      "size": 1234567890
    }
  ]
}
```

## 参数说明

### 通用参数
- `model`: 模型名称，如 "qwen2.5:7b"
- `stream`: 是否流式输出，建议设为 false

### Chat API 参数
- `messages`: 消息数组，包含 role 和 content
  - `role`: "system", "user", "assistant"
  - `content`: 消息内容
- `options`: 可选配置
  - `temperature`: 创造性 (0.0-2.0)，默认 0.7
  - `num_predict`: 最大输出长度，默认 2048
  - `top_k`: 词汇选择范围，默认 40
  - `top_p`: 核采样，默认 0.9
  - `repeat_penalty`: 重复惩罚，默认 1.1

### Generate API 参数
- `prompt`: 输入提示词
- `context`: 上下文数组（可选）
- `options`: 同 Chat API

## 最佳实践

### 1. 系统提示词设计
```json
{
  "role": "system",
  "content": "你是一个专业的零食商城客服助手，能够帮助用户解答关于商品、订单、支付、退换货等问题。请用友好、专业的语气回答用户问题。回答要准确、简洁、有用。"
}
```

### 2. 上下文管理
- 保持对话历史在合理长度内
- 定期清理过长的上下文
- 使用系统提示词指导模型行为

### 3. 错误处理
- 检查 Ollama 服务状态
- 处理网络超时
- 提供降级方案（规则引擎）

### 4. 性能优化
- 选择合适的模型大小
- 调整 temperature 参数
- 使用缓存减少重复计算

## 故障排除

### 1. 连接失败
- 检查 Ollama 服务是否启动
- 确认端口 11434 是否被占用
- 检查防火墙设置

### 2. 模型不存在
```bash
# 查看可用模型
ollama list

# 下载模型
ollama pull qwen2.5:7b
```

### 3. 内存不足
- 使用更小的模型（3B 而不是 7B）
- 增加系统内存
- 关闭其他占用内存的应用

### 4. 响应质量差
- 调整 temperature 参数
- 优化系统提示词
- 检查模型是否完整下载

## 测试工具

使用 `test_ollama_api.html` 文件测试 API 调用：

1. 在浏览器中打开文件
2. 测试连接状态
3. 测试 Chat API
4. 测试 Generate API
5. 查看模型列表

## 集成到系统

### 1. 配置
在 `application.yml` 中配置：
```yaml
chatbot:
  ollama-url: http://localhost:11434
  model: qwen2.5:7b
  max-tokens: 2048
  temperature: 0.7
```

### 2. 服务调用
```java
// 构建请求
Map<String, Object> requestBody = new HashMap<>();
requestBody.put("model", chatbotConfig.getModel());
requestBody.put("messages", messages);
requestBody.put("stream", false);

// 调用 API
String response = webClient.post()
    .uri(chatbotConfig.getOllamaUrl() + "/api/chat")
    .bodyValue(requestBody)
    .retrieve()
    .bodyToMono(String.class)
    .block();
```

### 3. 响应解析
```java
// 解析 Chat API 响应
if (response.contains("\"message\":")) {
    int messageStart = response.indexOf("\"message\":");
    int contentStart = response.indexOf("\"content\":", messageStart);
    if (contentStart != -1) {
        int contentValueStart = response.indexOf("\"", contentStart + 10) + 1;
        int contentValueEnd = response.indexOf("\"", contentValueStart);
        if (contentValueStart != -1 && contentValueEnd != -1) {
            String responseContent = response.substring(contentValueStart, contentValueEnd);
            return responseContent.trim();
        }
    }
}
```

## 注意事项

1. **资源要求**: 7B 模型需要至少 8GB 内存
2. **首次启动**: 模型加载可能需要几分钟
3. **网络要求**: 首次下载模型需要稳定的网络连接
4. **存储空间**: 模型文件占用约 4-8GB 磁盘空间
5. **API 版本**: 优先使用 Chat API，Generate API 为兼容性保留
