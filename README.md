# Develop on EC2 instances

When you're an occasional developer playing around with a wide range of technologies, it's just too much pain to 

1. keep virtualbox always working so you have a decent Linux environment
2. Keep your VM management tools in order on your local system (Vagrant, boot2docker, etc.)
3. Reboot your laptop when the VM crashes it or requires it to keep working
4. Watch your laptop grind to a halt and disrupt other work as you play with fun new things that actually consume resources

Let's bite the bullet and keep nice tidy scripts to do all the things on AWS. Dependencies:

1. AWS CLI installed
2. jq installed
3. AWS CLI has a `devbox` profile with an IAM user that can create cloudformation stacks and do EC2 stuff

# Usage

```
setup       First thing to run and only run once. Sets up the basic 
            infrastructure with cloudformation. No running costs.

cleanup     Destroy cloudformation. **Does not clean up everything, WIP**

boot        Boot an instance from an image. Hourly charges apply after this.

destroy     Terminate an instance. No more charges except snapshots

start       Start a stopped instance.

stop        Stop a running instance preserving file system for later start 
            without snapshotting. EBS volume costs still apply.

snapshot    Snapshot the file system of a running instance as an image that 
            can be passed to boot. S3 costs apply.

images      List all images.

instances   List all instances.

connect     Connect to an instance

resize      Resize an instance

info        Convenience command for listing both images and instances

```

