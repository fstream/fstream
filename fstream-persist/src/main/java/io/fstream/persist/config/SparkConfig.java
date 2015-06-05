/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.fstream.persist.config;

import static scala.collection.JavaConversions.asScalaMap;
import io.fstream.persist.config.PersistProperties.SparkProperties;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Spark configuration.
 * <p>
 * See annotation documentation for details.
 */
@Slf4j
@Lazy
@Configuration
public class SparkConfig {

  /**
   * Dependencies.
   */
  @Autowired
  SparkProperties spark;

  @Bean
  public SparkConf sparkConf() {
    log.info("Creating SparkConf with spark properties '{}'", spark);
    return new SparkConf()
        .setAppName("fstream-persist")
        .setMaster(spark.getMaster())
        .setAll(asScalaMap(spark.getProperties()));
  }

  @Bean(destroyMethod = "stop")
  public JavaSparkContext sparkContext() {
    log.info("Creating JavaSparkContext...");
    val sparkContext = new JavaSparkContext(sparkConf());

    val jobJar = getJobJar();
    if (jobJar != null) {
      log.info("Adding job jar: {}", jobJar);
      sparkContext.addJar(jobJar);
    }

    return sparkContext;
  }

  private static String getJobJar() {
    val jarAnchor = SparkConfig.class;
    val path = getPath(jarAnchor);

    return isExpoded(path) ? null : path;
  }

  private static boolean isExpoded(String path) {
    return path.contains("/bin") || path.contains("/build");
  }

  private static String getPath(Class<?> type) {
    return type.getProtectionDomain().getCodeSource().getLocation().getPath();
  }

}
