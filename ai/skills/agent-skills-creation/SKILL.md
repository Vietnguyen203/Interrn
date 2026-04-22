---
name: agent-skills-creation
description: Hướng dẫn tạo Agent Skills theo chuẩn agentskills.io. Sử dụng khi cần tạo skill mới cho AI agent, đóng gói kiến thức/quy trình thành skill tái sử dụng, hoặc cần hiểu về cấu trúc và format của Agent Skills.
license: MIT
metadata:
  author: hoangnguyenvan
  version: "1.0"
  source: https://agentskills.io/specification
---

# Agent Skills Creation Guide

Skill này cung cấp hướng dẫn để tạo các Agent Skills theo chuẩn https://agentskills.io/specification.

## Khi nào sử dụng

- Cần tạo skill mới cho AI agent
- Muốn đóng gói kiến thức hoặc quy trình thành skill tái sử dụng
- Cần tham khảo format và cấu trúc chuẩn của Agent Skills

## Cấu trúc thư mục

```
skill-name/
├── SKILL.md          # Bắt buộc - File chính chứa hướng dẫn
├── scripts/          # Tùy chọn - Các script hỗ trợ
├── references/       # Tùy chọn - Tài liệu tham khảo
└── assets/           # Tùy chọn - Templates, hình ảnh, data files
```

## Định dạng SKILL.md

### 1. Frontmatter (Bắt buộc)

```yaml
---
name: skill-name
description: Mô tả skill làm gì và khi nào nên dùng (1-1024 ký tự)
license: Apache-2.0                    # Tùy chọn
compatibility: Requires git, docker    # Tùy chọn (1-500 ký tự)
metadata:                              # Tùy chọn
  author: example-org
  version: "1.0"
allowed-tools: Bash(git:*) Read        # Tùy chọn - Thử nghiệm
---
```

### 2. Quy tắc đặt tên (name field)

- 1-64 ký tự
- Chỉ dùng lowercase `a-z` và dấu gạch ngang `-`
- Không bắt đầu hoặc kết thúc bằng `-`
- Không có `--` liên tiếp
- **Phải khớp với tên thư mục cha**

**Đúng:**
- `pdf-processing`
- `data-analysis`
- `code-review`

**Sai:**
- `PDF-Processing` (uppercase không được phép)
- `-pdf` (không được bắt đầu bằng `-`)
- `pdf--processing` (không được có `--`)

### 3. Quy tắc mô tả (description field)

- 1-1024 ký tự
- Mô tả cả **skill làm gì** và **khi nào nên dùng**
- Bao gồm keywords giúp agent nhận diện tasks phù hợp

**Tốt:**
```yaml
description: Extracts text and tables from PDF files, fills PDF forms, and merges multiple PDFs. Use when working with PDF documents or when the user mentions PDFs, forms, or document extraction.
```

**Không tốt:**
```yaml
description: Helps with PDFs.
```

### 4. Body Content

Nội dung chính nên bao gồm:
- **Hướng dẫn từng bước** (step-by-step instructions)
- **Ví dụ** inputs và outputs
- **Edge cases** phổ biến

## Thư mục tùy chọn

### scripts/
Chứa các script có thể thực thi:
- Nên self-contained hoặc document rõ dependencies
- Bao gồm error messages hữu ích
- Xử lý edge cases gracefully

### references/
Chứa tài liệu tham khảo:
- `REFERENCE.md` - Technical reference chi tiết
- `FORMS.md` - Form templates hoặc structured data formats
- Domain-specific files (`finance.md`, `legal.md`...)

### assets/
Chứa các tài nguyên:
- Templates (document templates, configuration templates)
- Images (diagrams, examples)
- Data files (lookup tables, schemas)

## Progressive Disclosure

Agent Skills sử dụng cơ chế tiết lộ dần để tối ưu token usage:

1. **Metadata (~100 tokens)**: `name` và `description` load khi khởi động
2. **Instructions (<5000 tokens recommended)**: Full `SKILL.md` body load khi skill kích hoạt
3. **Resources (as needed)**: Files trong `scripts/`, `references/`, `assets/` chỉ load khi cần

> **Khuyến nghị**: Giữ SKILL.md body dưới 5000 tokens

## Tham chiếu file trong SKILL.md

```markdown
See [the reference guide](references/REFERENCE.md) for details.
Run the extraction script: scripts/extract.py
```

## Validation

Dùng công cụ `skills-ref` để validate:

```bash
skills-ref validate ./my-skill
```

## Ví dụ hoàn chỉnh

```yaml
---
name: pdf-processing
description: Extract text and tables from PDF files, fill PDF forms, and merge multiple PDFs. Use when working with PDF documents or when the user mentions PDFs, forms, or document extraction.
license: Apache-2.0
compatibility: Requires Python 3.8+
metadata:
  author: example-org
  version: "1.0"
---

# PDF Processing Skill

## When to use
- User needs to extract text from PDF
- User wants to fill PDF forms
- User needs to merge multiple PDFs

## Steps
1. Read the PDF file
2. Use scripts/extract.py for text extraction
3. Return structured data

## Examples
Input: "Extract text from document.pdf"
Output: Extracted text content...

## Edge Cases
- Encrypted PDFs require password
- Scanned PDFs need OCR processing
```

## Tài liệu tham khảo

- [Agent Skills Specification](https://agentskills.io/specification)
- [What are skills?](https://agentskills.io/what-are-skills)
- [Integrate skills](https://agentskills.io/integrate-skills)
- [GitHub: skills-ref](https://github.com/agentskills/agentskills/tree/main/skills-ref)
