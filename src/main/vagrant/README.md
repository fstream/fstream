fStream - Vagrant VirtualBox VM
===

Follow the directions outlined in this document to create, start, provision and export the fStream virtual machine (VM). Provisioning is configured in [`../ansible`](ansible) .

Setup
---
The minimum requirements for creating the VM is VirtualBox (4.3+), Vagrant (1.3.5+) and Ansible (1.4+). You can find instructions on how to install each of these by following the links below.

## General:

- Install [VirtualBox](https://www.virtualbox.org/wiki/Downloads)
- Install [Vagrant](http://downloads.vagrantup.com)
- Install [Ansible](http://www.ansibleworks.com/docs/intro_installation.html)

## MacOS:

- Install Vagrant and VirtualBox http://www.sourabhbajaj.com/mac-setup/Vagrant/README.html
- Install Ansible https://devopsu.com/guides/ansible-mac-osx.html

*Note*: It is important that you _not_ install the external [`vagrant-anisble`](https://github.com/dsander/vagrant-ansible) plugin as this is deprecated and is already bundled with recent versions of Vagrant. Doing so may result in `python` Ansible class loading issues.

Provisioning
---
To create and provision the VM, clone the project, navigate to the `vagrant` directory and issue [`vagrant up`](http://docs.vagrantup.com/v2/cli/up.html):
 
 	git clone <project>
 	cd fstream/fstream/src/main/vagrant/aws
	vagrant up

Ad Hoc Commands
---
Sometimes it may be convenient to issue [ad-hoc commands](http://www.ansibleworks.com/docs/intro_adhoc.html); something that you might type in to do something really quick, but don’t want to save for later. For example, in the [Vagrant environment](http://www.ansibleworks.com/docs/guide_vagrant.html#id5) you may issue the following command to print `$PWD`:

	ansible all -i provisioning/inventory -u vagrant --private-key ~/.vagrant.d/insecure_private_key -m shell -a 'pwd'

Resources
---
When developing Ansible playbooks targeting the VirtualBox provider, you may find the following resources useful.

#### VirtualBox
- http://www.virtualbox.org/manual/ch08.html

#### Vagrant - VirtualBox Provider
- http://docs.vagrantup.com/v2/virtualbox/configuration.html

#### Vagrant - AWS Provider
- https://github.com/mitchellh/vagrant-aws

#### Vagrant - Ansible Provisioner
- http://www.ansibleworks.com/docs/guide_vagrant.html
- http://docs.vagrantup.com/v2/provisioning/ansible.html

#### Ansible
- http://www.ansibleworks.com/docs
- http://www.ansibleworks.com/docs/playbooks_best_practices.html

#### Ansible - Templates (Jinja2)
- http://jinja.pocoo.org/docs/templates
