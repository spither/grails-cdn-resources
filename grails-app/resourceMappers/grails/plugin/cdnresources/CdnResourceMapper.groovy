package grails.plugin.cdnresources

import org.grails.plugin.resource.mapper.MapperPhase
import org.springframework.web.context.request.RequestContextHolder

class CdnResourceMapper {

    static priority = 15000 // after resources have been zipped, cached + properly beaten
    static phase = MapperPhase.DISTRIBUTION

    def grailsApplication

    def map(resource, config) {

        def mergedConfig = config + grailsApplication.config.grails.resources.cdn

        if(mergedConfig.enabled) {

            def url
            def httpsUrl

            if(resource.module?.name && mergedConfig.moduleUrls[resource.module.name]) {
                url = mergedConfig.moduleUrls[resource.module.name]
            }
            if(resource.module?.name && mergedConfig.moduleHttpsUrls[resource.module.name]) {
                httpsUrl = mergedConfig.moduleHttpsUrls[resource.module.name]
            }

            if(!url) {
                url = mergedConfig.url
                if(url?.endsWith('/')) {
                    url = url[0..-2]
                }
            }
            if(!httpsUrl) {
                httpsUrl = mergedConfig.httpsUrl
                if(httpsUrl?.endsWith('/')) {
                    httpsUrl = httpsUrl[0..-2]
                }
            }

            if(url) {
                def linkUrl = resource.linkUrl
                if(httpsUrl && httpsUrl != url) {
                    resource.dynamicLinkOverride = {
                        def secure = false
                        def req = RequestContextHolder?.requestAttributes?.request
                        if(req) {
                            // TODO Collect values on the next line from config, and check useHeaderCheckChannelSecurity is enabled
                            if(req.isSecure() || 'https'.equals(req.getHeader('X-Forwarded-Proto'))) {
                                secure = true
                            }
                        }
                        return (secure ? httpsUrl : url) + linkUrl
                    }
                }
                else {
                    resource.linkOverride = url + linkUrl
                }
            }
        }
    }
}
