FROM docker.io/library/python:3.11-alpine@sha256:2a068b9442f61f4480306d44e3b166bfe3343761e9bd57c38f66302ebf28fd9e
WORKDIR /opt/tests
# hadolint ignore=DL3018
RUN apk --no-cache add git

COPY requirements.txt .
RUN pip install --require-hashes --no-cache-dir -r requirements.txt
COPY . .

ENTRYPOINT ["pytest"]
CMD [ "test.py" ]
