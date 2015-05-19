# Copyright (c) 2015 fStream. All Rights Reserved.
#
# fStream - Ansible filters
#
# Description: 
#   Custom Ansible filters
#
# See:
#   - http://docs.ansible.com/developing_plugins.html#filter-plugins

def group_ips(hostvars, groups, group_name):
    return [hostvars[host]['ansible_eth0']['ipv4']['address'] for host in groups[group_name]]

class FilterModule(object):
    ''' Utility filters '''

    def filters(self):
        return {
            'group_ips' : group_ips
        }
