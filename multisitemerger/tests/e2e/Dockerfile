FROM python:3.8-alpine
WORKDIR /opt/tests
# hadolint ignore=DL3018
RUN apk --no-cache add git

COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .

ENTRYPOINT ["pytest"]
CMD [ "test.py" ]
