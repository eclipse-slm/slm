var serviceInstanceMixin = {
    methods: {
        getServiceOfferingText (serviceInstance) {
            let serviceOffering = this.serviceOfferingById(serviceInstance.serviceOfferingId);
            if(serviceOffering === undefined){
                return "";
            }
            let serviceOfferingVersionText = serviceOffering.versions.find(version => version.id === serviceInstance.serviceOfferingVersionId)?.version

            return `${serviceOffering.name} (Version: ${serviceOfferingVersionText})`;
        },
        getPortLinks (service) {
            const resource = this.resourceById(service.resourceId);
            const portLinks = [];
            if (service.metaData.ports != null && resource != undefined) {
                JSON.parse(service.metaData.ports).forEach(port => {
                    const portLink = `http://${resource.ip}:${port}`;
                    portLinks.push({ text: port, link: portLink });
                });

                return portLinks;
            } else {
                return [];
            }
        }
    }
};

export { serviceInstanceMixin };
