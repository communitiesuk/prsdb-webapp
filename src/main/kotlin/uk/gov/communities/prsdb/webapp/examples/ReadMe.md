# Connecting to AWS S3 locally
By default, when the service is run locally, it uses the `LocalFileUploader` instead of the `AwsS3FileUploader`.
You can manually switch by manipulating the profiles and attributes on those classes. 
Currently, there isn't a profile which connects to AWS with an otherwise local build.

## Connecting to AWS
When the service runs in AWS it has the profile of the ECS service it is running on. 
This allows it to connect to e.g. S3, the database and other AWS services.
To connect to the deployed database while running locally you need to set up a port forwarding session using SSM due to 
networking rules.
To connect to S3 you need to provide your local service with a profile with which to connect. 
You can do that using `aws-vault`, as follows. 
To set up `aws-vault` follow the instructions in the `prsdb-infra` repository.

## Setting up `aws-vault` as a profile server
Run 
```shell
aws-vault exec <profile> --server
```

This starts a session with aws-vault acting as a credential server. 
You can add `-- bash` or `-- powershell` to enter the server using your shell of choice.

Then run
```shell
env | grep AWS_CONTAINER
```

This will return two lines giving you the `AWS_CONTAINER_CREDENTIALS_FULL_URI` and the 
`AWS_CONTAINER_AUTHORIZATION_TOKEN` for your server.
Copy both of these lines into your `.env` file and add the line
```
AWS_REGION=eu-west-2
```

Then run the service as usual, it will pick up the profile provided by the `aws-vault exec` command.

When you have finished running the service, run `exit` in the server terminal to close the server.