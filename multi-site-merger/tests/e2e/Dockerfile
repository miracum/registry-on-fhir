FROM docker.io/library/python:3.11-alpine@sha256:3e73c0b6c1f0205225359acac5c8ab1852e7281248d72afda3e1bb8c6f47deab
WORKDIR /opt/tests
# hadolint ignore=DL3018
RUN apk --no-cache add git

COPY requirements.txt .
RUN pip install --require-hashes --no-cache-dir -r requirements.txt
COPY . .

ENTRYPOINT ["pytest"]
CMD [ "test.py" ]
