FROM node:20-alpine AS builder

WORKDIR /app

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/. .
RUN npx vite build

FROM node:20-alpine AS runner

WORKDIR /app

# Railway injects PORT at runtime. Default keeps local docker run simple.
ENV PORT=8080

RUN npm install -g serve

COPY --from=builder /app/dist ./dist

EXPOSE 8080

CMD ["sh", "-c", "serve -s dist -l ${PORT}"]
