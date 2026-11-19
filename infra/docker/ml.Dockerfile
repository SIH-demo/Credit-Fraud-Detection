
FROM python:3.11-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1 PIP_DISABLE_PIP_VERSION_CHECK=1

COPY ML/requirements.txt .

RUN --mount=type=cache,target=/root/.cache/pip pip install --prefer-binary -r requirements.txt

COPY ML/app/ .

RUN useradd --create-home appuser && chown -R appuser:appuser /app

USER appuser

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
