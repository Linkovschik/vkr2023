package com.example.demo.mvc

import jakarta.servlet.ServletContext
import jakarta.servlet.ServletException
import org.springframework.web.filter.HiddenHttpMethodFilter
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer

class WebMvcInitilizer : AbstractAnnotationConfigDispatcherServletInitializer() {

    @Throws(ServletException::class)
    override fun onStartup(aServletContext: ServletContext) {
        super.onStartup(aServletContext)
        registerHiddenFieldFilter(aServletContext)
    }

    override fun getServletMappings(): Array<String> {
        return arrayOf("/");
    }

    override fun getRootConfigClasses(): Array<Class<*>>? {
        return null;
    }

    override fun getServletConfigClasses(): Array<Class<*>>? {
        return arrayOf(SpringConfig::class.java)
    }

    private fun registerHiddenFieldFilter(aContext: ServletContext) {
        aContext.addFilter("hiddenHttpMethodFilter",
                HiddenHttpMethodFilter()).addMappingForUrlPatterns(null, true, "/*")
    }
}