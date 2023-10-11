FROM docker.io/library/python:3.12-alpine@sha256:ae35274f417fc81ba6ee1fc84206e8517f28117566ee6a04a64f004c1409bdac
WORKDIR /opt/tests
# hadolint ignore=DL3018
RUN apk --no-cache add git

COPY requirements.txt .
RUN pip install --require-hashes --no-cache-dir -r requirements.txt
COPY . .

ENTRYPOINT ["pytest"]
CMD [ "test.py" ]
