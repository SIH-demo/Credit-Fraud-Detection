
FROM python:3.11-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1 PIP_DISABLE_PIP_VERSION_CHECK=1

COPY Analytics/requirements.txt .

RUN --mount=type=cache,target=/root/.cache/pip pip install --prefer-binary -r requirements.txt

COPY Analytics/app/ .

RUN useradd --create-home appuser && chown -R appuser:appuser /app

USER appuser

EXPOSE 8001

CMD ["uvicorn", "analyze:app", "--host", "0.0.0.0", "--port", "8001"]
