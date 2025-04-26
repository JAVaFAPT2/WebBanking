using Autofac;
using Autofac.Extensions.DependencyInjection;
using Infrastructure.Persistence.DBContext;
using Microsoft.EntityFrameworkCore;
using UserService;

namespace Presentation
{
    public class Program
    {
        public static void Main(string[] args)
        {
            // 1. Create the builder
            var builder = WebApplication.CreateBuilder(args);

            // 2. Register framework services **before** Build()
            builder.Services.AddControllers();                                    // controllers-based API :contentReference[oaicite:4]{index=4}
            builder.Services.AddEndpointsApiExplorer();                           // minimal APIs explorer :contentReference[oaicite:5]{index=5}
            builder.Services.AddSwaggerGen();                                     // Swagger/OpenAPI :contentReference[oaicite:6]{index=6}

            // 3. Register EF Core DbContext **before** Build()
            builder.Services.AddDbContext<ApplicationDbContext>(options =>
                options.UseSqlServer(
                    builder.Configuration.GetConnectionString("DefaultConnection")
                )
            );                                                                   // adds DbContextOptions<T> :contentReference[oaicite:7]{index=7}

            // 4. Configure Autofac as the DI container **before** Build()
            builder.Host.UseServiceProviderFactory(new AutofacServiceProviderFactory());
            // plug in Autofac :contentReference[oaicite:8]{index=8}
            builder.Host.ConfigureContainer<ContainerBuilder>(containerBuilder =>
            {
                containerBuilder.AddGenericHandlers();                            // your MediatR + repo registrations :contentReference[oaicite:9]{index=9}
            });

            // 5. Build the application
            var app = builder.Build();

            // 6. Configure middleware & endpoints
            if (app.Environment.IsDevelopment())
            {
                app.UseSwagger();                                                // Swagger middleware :contentReference[oaicite:10]{index=10}
                app.UseSwaggerUI();
            }

            app.UseHttpsRedirection();                                          // HTTPS redirection :contentReference[oaicite:11]{index=11}
            app.UseAuthorization();                                             // auth middleware :contentReference[oaicite:12]{index=12}

            app.MapControllers();                                               // map attribute-routed controllers :contentReference[oaicite:13]{index=13}

            // 7. Run!
            app.Run();                                                          // start the host :contentReference[oaicite:14]{index=14}
        }
    }
}
